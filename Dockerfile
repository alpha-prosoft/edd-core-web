ARG DOCKER_URL
ARG DOCKER_ORG

# ===========================================================================
# Stage 1: primitives — lint, unit tests, compile, release the static gallery,
# build the published jars and the integration-test uberjar.
# Runs on web-img (clojure + node + maven toolchain).
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/web-img:latest AS primitives

ARG ARTIFACT_ORG
ARG BUILD_ID
# APP_VERSION comes from the pipeline, see version.sh.
ARG APP_VERSION=0.0.${BUILD_ID}
ARG VERSION=${APP_VERSION}

USER root
RUN node -v && npm -v && npm config get registry && \
    npm install -g --loglevel verbose --fetch-timeout=60000 --fetch-retries=2 \
      shadow-cljs karma karma-cljs-test karma-chrome-launcher karma-junit-reporter
USER build

COPY --chown=build:build modules/edd-web-primitives/shadow-cljs.edn shadow-cljs.edn
COPY --chown=build:build modules/edd-web-primitives/deps.edn deps-temp.edn
COPY --chown=build:build modules/edd-web-primitives/tests.edn tests.edn
COPY --chown=build:build modules/edd-core-web-playwright /opt/edd-core-web-playwright
RUN sed -i 's#"../edd-core-web-playwright"#"/opt/edd-core-web-playwright"#g' deps-temp.edn

ARG DEPLOY_TARGET
ENV DEPLOY_TARGET=${DEPLOY_TARGET}

RUN mkdir -p /dist/s3
RUN set -e &&\
    clojure -M merge.clj &&\
    npx shadow-cljs classpath

COPY --chown=build:build modules/edd-web-primitives/resources resources
COPY --chown=build:build modules/edd-web-primitives/src src
COPY --chown=build:build modules/edd-web-primitives/build.clj build.clj
COPY --chown=build:build modules/edd-web-primitives/tests-it.edn tests-it.edn
COPY --chown=build:build modules/edd-web-primitives/.clj-kondo .clj-kondo
COPY --chown=build:build modules/edd-web-primitives/check-suneditor-vendor.sh check-suneditor-vendor.sh
COPY --chown=build:build modules/edd-web-widgets/src/it widgets-it

RUN set -e && clojure -M:lint --lint src/main src/shared src/test src/gallery src/sdk src/it
RUN set -e && clojure -M:test:runner

# shadow-cljs 3.5+ no longer installs npm deps implicitly on every command;
# install the versions pinned in src/main/deps.cljs before compiling.
RUN set -e && npx shadow-cljs run shadow.cljs.npm-deps
RUN set -e && bash check-suneditor-vendor.sh
# ClojureScript unit tests; the bundle runs in a browser in the uitest stage.
RUN set -e && npx shadow-cljs -A:test compile test &&\
    mkdir -p /dist/karma/primitives/target &&\
    cp target/ci.js /dist/karma/primitives/target/ci.js
RUN set -e && npx shadow-cljs -A:dev:gallery compile gallery

# The UI SDK must compile on its own (no gallery / cljs on the classpath).
RUN set -e && clojure -M:sdk -e "(require 'web.primitives.sdk.core 'web.primitives.sdk.button 'web.primitives.sdk.checkbox 'web.primitives.sdk.switch 'web.primitives.sdk.text-field 'web.primitives.sdk.select 'web.primitives.sdk.autocomplete 'web.primitives.sdk.radio 'web.primitives.sdk.slider 'web.primitives.sdk.date-picker 'web.primitives.sdk.text-editor 'web.primitives.sdk.dialog 'web.primitives.sdk.snackbar 'web.primitives.sdk.menu 'web.primitives.sdk.tabs 'web.primitives.sdk.drawer 'web.primitives.sdk.accordion 'web.primitives.sdk.toggle-button 'web.primitives.sdk.tooltip 'web.primitives.sdk.display 'web.primitives.sdk.console)"

RUN set -e &&\
    clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.9.2"}}}' \
            -M -m cljfmt.main check src/main/ src/shared src/test src/gallery src/sdk src/it

RUN set -e && npx shadow-cljs -A:gallery release gallery
RUN cp -r resources/gallery/* /dist/s3/
RUN sed -i 's/version=1/version='${VERSION}'/g' /dist/s3/index.html

# Packaged, self-contained integration-test runner (SDK + edd-core-web-playwright +
# IT suites of primitives and widgets). Runs later on ui-runtime with plain `java -jar`.
RUN set -e && clojure -T:build it-uberjar :out '"/dist/it"' :extra-src-dirs '["widgets-it"]' &&\
    ls -la /dist/it

# Two published artifacts:
#   edd-web-primitives          - the primitives (cljs source jar)
#   edd-web-primitives-ui-sdk   - the Playwright UI test SDK (clj source jar)
RUN set -e && clojure -T:build all \
                      :group   "\"${ARTIFACT_ORG}\"" \
                      :version "\"${VERSION}\"" \
                      :out     "\"/dist/release-libs\"" &&\
    ls -la /dist/release-libs

# ===========================================================================
# Stage 2: widgets — release the widgets gallery, built on the primitives
# Gallery and served to the same IT runner in the uitest stage.
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/web-img:latest AS widgets

COPY --chown=build:build deps.edn edd/deps.edn
COPY --chown=build:build shadow-cljs.edn edd/shadow-cljs.edn
COPY --chown=build:build src edd/src
COPY --chown=build:build test edd/test
COPY --chown=build:build modules/edd-web-primitives edd/modules/edd-web-primitives
COPY --chown=build:build modules/edd-web-widgets edd/modules/edd-web-widgets

RUN set -e &&\
    cd edd/modules/edd-web-widgets &&\
    clojure -M:shadow-cljs run shadow.cljs.npm-deps &&\
    clojure -M:shadow-cljs:gallery release gallery &&\
    mkdir -p /dist/widgets-gallery &&\
    cp -r resources/gallery/* /dist/widgets-gallery/

# edd-core-web ClojureScript unit tests, run in a browser in the uitest stage.
RUN set -e &&\
    cd edd &&\
    clojure -M:shadow-cljs:test run shadow.cljs.npm-deps &&\
    clojure -M:shadow-cljs:test compile test &&\
    mkdir -p /dist/karma/core/target &&\
    cp target/ci.js /dist/karma/core/target/ci.js

# ===========================================================================
# Stage 3: uitest — run the Playwright suite on ui-runtime.
# ui-runtime is the official Playwright image (browsers in /ms-playwright)
# plus Java; the gallery is served as a plain static site and the tests run
# from the uberjar. Its Playwright version must match the one in
# modules/edd-web-primitives/deps.edn.
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/ui-runtime-img:latest AS uitest

USER root
WORKDIR /opt/uitest

COPY --from=primitives /dist/it/it-tests.jar it-tests.jar
COPY --from=primitives /dist/s3 gallery
COPY --from=widgets /dist/widgets-gallery widgets-gallery
COPY modules/edd-web-primitives/run-it.sh run-it.sh
COPY --from=primitives /dist/karma karma
COPY --from=widgets /dist/karma karma
COPY karma.conf.js karma/primitives/karma.conf.js
COPY karma.conf.js karma/core/karma.conf.js

ENV IT_JAR=/opt/uitest/it-tests.jar
ENV GALLERY_DIR=/opt/uitest/gallery
ENV GALLERY_PORT=3002

# ClojureScript unit tests of primitives and core in Playwright's Chromium.
RUN set -e &&\
    cd karma &&\
    npm install --no-save --no-audit --no-fund karma karma-cljs-test karma-chrome-launcher karma-junit-reporter &&\
    export CHROME_BIN=$(ls -d /ms-playwright/chromium-*/chrome-linux*/chrome | head -1) &&\
    for suite in primitives core; do \
      echo "ClojureScript unit tests: $suite" &&\
      npx karma start $suite/karma.conf.js --single-run || exit 1; \
    done

RUN set -e &&\
    bash run-it.sh &&\
    GALLERY_DIR=/opt/uitest/widgets-gallery GALLERY_PORT=3009 \
      bash run-it.sh web.gallery.it.smoke-test web.widgets.gallery.it.widgets-test &&\
    date > /opt/uitest/it-passed

# ===========================================================================
# Stage 4: edd-core-web and the remaining modules, built against the
# primitives jar from stage 1. Depends on a green UI test run.
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/common-img:b681

ARG ARTIFACT_ORG
ENV PROJECT_NAME=edd-core-web

# Custom build here
COPY src src
COPY test test
COPY --chown=build:build deps.edn deps.edn
COPY modules/edd-web-primitives modules/edd-web-primitives

RUN set -e && clojure -A:test:unit

ARG BUILD_ID
ARG APP_VERSION=0.0.${BUILD_ID}
ARG VERSION=${APP_VERSION}

COPY --from=uitest /opt/uitest/it-passed /tmp/.it-passed
COPY --from=primitives --chown=build:build /dist/release-libs /dist/release-libs
COPY --from=primitives --chown=build:build /dist/s3 /dist/s3

RUN set -e &&\
    for i in edd-web-primitives edd-web-primitives-ui-sdk; do \
      mvn install:install-file \
         -Dfile=/dist/release-libs/${i}-${VERSION}.jar \
         -DgroupId=${ARTIFACT_ORG} \
         -DartifactId=${i} \
         -DpomFile=/dist/release-libs/${i}-${VERSION}.jar.pom.xml \
         -Dversion="${VERSION}" \
         -Dpackaging=jar || exit 1; \
    done

COPY --chown=build:build modules modules
COPY --chown=build:build templates templates

# We copy from Jenkinsfile repo here (cbd-jenkins-pipeline)
COPY --chown=build:build scripts/build.clj build.clj

RUN set -e &&\
    echo "Building ${VERSION}" &&\
    root=$PWD &&\
    module_names="$(ls modules)" &&\
    modules=$(find modules/  -name "deps.edn" -exec dirname {} \;) &&\
    modules="$modules templates/project ." &&\
    libs="$(ls -xm modules), edd-web-primitives-ui-sdk, ${PROJECT_NAME}" &&\
    for i in $modules; do \
      echo "Moving to $i" &&\
      cd $i &&\
      echo "Updating libs: $libs" &&\
      bb -i '(doseq [lib-name (clojure.string/split "'"$(echo $libs)"'" #", ")] \
              (let [version "'${VERSION}'" \
                    group-id "'${ARTIFACT_ORG}'" \
                    lib (symbol (str "edd/" lib-name)) \
                    new-lib (symbol (str group-id "/" lib-name)) \
                    deps (read-string \
                          (slurp (io/file "deps.edn"))) \
                    global (get-in deps [:deps lib]) \
                    deps (if global \
                           (-> deps \
                             (update-in [:deps] #(dissoc % lib))  \
                             (assoc-in [:deps new-lib] {:mvn/version version})) \
                        deps) \
                    aliases (keys (:aliases deps)) \
		    deps (update deps :aliases #(dissoc % :local-dev)) \
                    deps (reduce \
                            (fn [p alias] \
                             (if (get-in p [:aliases alias :extra-deps lib]) \
                               (-> p \
                                  (update-in [:aliases alias :extra-deps] #(dissoc % lib))  \
                                  (assoc-in [:aliases alias :extra-deps new-lib] \
                                            {:mvn/version version})) \
                               p)) \
                           deps \
                           aliases)] \
                (spit "deps.edn" (with-out-str \
                                  (clojure.pprint/pprint deps)))))' &&\
      cd $root || exit 1; \
    done &&\
    cd $root &&\
    mkdir -p resources &&\
    clojure -M:zip &&\
    ls -la resources &&\
    clojure -T:build jar   \
             :group-id "\"${ARTIFACT_ORG}\"" \
             :artifact-id "\"${PROJECT_NAME}\"" \
             :version "\"${VERSION}\"" &&\
    cp target/classes/META-INF/maven/${ARTIFACT_ORG}/${PROJECT_NAME}/pom.xml \
             /dist/release-libs/${PROJECT_NAME}-${VERSION}.jar.pom.xml &&\
    cp target/${PROJECT_NAME}-${VERSION}.jar /dist/release-libs/${PROJECT_NAME}-${VERSION}.jar &&\
    mvn install:install-file \
       -Dfile=target/${PROJECT_NAME}-${VERSION}.jar \
       -DgroupId=${ARTIFACT_ORG} \
       -DartifactId=${PROJECT_NAME} \
       -DpomFile=target/classes/META-INF/maven/${ARTIFACT_ORG}/${PROJECT_NAME}/pom.xml \
       -Dversion="${VERSION}" \
       -Dpackaging=jar &&\
    echo "Building modules:" &&\
    for i in edd-web-widgets $(ls modules | grep -vxE 'edd-web-primitives|edd-web-widgets'); do  \
      echo "Moving to modules/$i" &&\
      cd modules/$i &&\
      cp $root/build.clj . &&\
      clojure -T:build jar   \
             :group-id "\"${ARTIFACT_ORG}\"" \
             :artifact-id "\"${i}\"" \
             :version "\"${VERSION}\"" &&\
      cp target/classes/META-INF/maven/${ARTIFACT_ORG}/${i}/pom.xml \
             /dist/release-libs/${i}-${VERSION}.jar.pom.xml &&\
      cp target/${i}-${VERSION}.jar /dist/release-libs/${i}-${VERSION}.jar &&\
      mvn install:install-file \
         -Dfile=target/${i}-${VERSION}.jar \
         -DgroupId=${ARTIFACT_ORG} \
         -DartifactId=${i} \
         -DpomFile=target/classes/META-INF/maven/${ARTIFACT_ORG}/${i}/pom.xml \
         -Dversion="${VERSION}" \
         -Dpackaging=jar &&\
      cd $root || exit 1; \
    done


RUN ls -la target
