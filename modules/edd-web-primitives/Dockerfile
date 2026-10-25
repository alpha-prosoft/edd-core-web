ARG DOCKER_URL
ARG DOCKER_ORG
ARG BUILD_ID
ARG PROJECT_NAME=edd-web-primitives

# ===========================================================================
# Stage 1: build — lint, unit tests, compile, release the static gallery,
# build the published jars and the integration-test uberjar.
# Runs on web-img (clojure + node + maven toolchain).
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/web-img:latest AS build

ENV PROJECT_NAME edd-web-primitives

ARG ARTIFACT_ORG
ARG BUILD_ID

# Custom build from here on

USER root
RUN node -v && npm -v && npm config get registry && \
    npm install -g --loglevel verbose --fetch-timeout=60000 --fetch-retries=2 \
      shadow-cljs karma karma-cljs-test karma-chrome-launcher karma-junit-reporter
USER build

COPY --chown=build:build shadow-cljs.edn shadow-cljs.edn
COPY --chown=build:build deps.edn deps-temp.edn
COPY --chown=build:build tests.edn tests.edn

RUN ls -la

ARG DEPLOY_TARGET
ENV DEPLOY_TARGET ${DEPLOY_TARGET}

RUN mkdir -p /dist/s3
RUN ls -la /dist
RUN set -e &&\
    clojure -M merge.clj &&\
    npx shadow-cljs classpath


COPY --chown=build:build resources resources
COPY --chown=build:build src src
COPY --chown=build:build build.clj build.clj
COPY --chown=build:build tests-it.edn tests-it.edn
COPY --chown=build:build .clj-kondo .clj-kondo
COPY --chown=build:build check-suneditor-vendor.sh check-suneditor-vendor.sh

RUN set -e && clojure -M:lint --lint src/main src/shared src/test src/gallery src/sdk src/it
RUN set -e && clojure -M:test:runner

# shadow-cljs 3.5+ no longer installs npm deps implicitly on every command;
# install the versions pinned in src/main/deps.cljs before compiling.
RUN set -e && npx shadow-cljs run shadow.cljs.npm-deps
RUN set -e && bash check-suneditor-vendor.sh
RUN set -e && npx shadow-cljs -A:dev:gallery compile gallery

# The UI SDK must compile on its own (no gallery / cljs on the classpath).
RUN set -e && clojure -M:sdk -e "(require 'web.primitives.sdk.core 'web.primitives.sdk.button 'web.primitives.sdk.checkbox 'web.primitives.sdk.switch 'web.primitives.sdk.text-field 'web.primitives.sdk.select 'web.primitives.sdk.autocomplete 'web.primitives.sdk.radio 'web.primitives.sdk.slider 'web.primitives.sdk.date-picker 'web.primitives.sdk.text-editor 'web.primitives.sdk.dialog 'web.primitives.sdk.snackbar 'web.primitives.sdk.menu 'web.primitives.sdk.tabs 'web.primitives.sdk.drawer 'web.primitives.sdk.accordion 'web.primitives.sdk.toggle-button 'web.primitives.sdk.tooltip 'web.primitives.sdk.display 'web.primitives.sdk.console)"

RUN set -e &&\
    clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.9.2"}}}' \
            -M -m cljfmt.main check src/main/ src/shared src/test src/gallery src/sdk src/it

RUN ls -la /dist
RUN set -e && npx shadow-cljs -A:gallery release gallery

RUN ls -la /dist
RUN cp -r resources/gallery/* /dist/s3/
RUN sed -i 's/version=1/version='${BUILD_ID}'/g' /dist/s3/index.html
RUN ls -la /dist

# Packaged, self-contained integration-test runner (SDK + clojure-playwright +
# IT suite). Runs later on ui-runtime with plain `java -jar`.
RUN set -e && clojure -T:build it-uberjar :out '"/dist/it"' &&\
    ls -la /dist/it

# Two published artifacts:
#   ${PROJECT_NAME}          - the primitives (cljs source jar)
#   ${PROJECT_NAME}-ui-sdk   - the Playwright UI test SDK (clj source jar)
RUN set -e && clojure -T:build all \
                      :group   "\"${ARTIFACT_ORG}\"" \
                      :version "\"1.0.${BUILD_ID}\"" \
                      :out     "\"/dist/release-libs\"" &&\
    ls -la /dist/release-libs

# ===========================================================================
# Stage 2: uitest — run the Playwright suite on ui-runtime.
# ui-runtime ships Chrome's system libraries + Java; the gallery is served as
# a plain static site and the tests run from the uberjar.
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/ui-runtime-img:latest AS uitest

USER root
WORKDIR /opt/uitest

COPY --from=build /dist/it/it-tests.jar it-tests.jar
COPY --from=build /dist/s3 gallery
COPY run-it.sh run-it.sh

ENV IT_JAR=/opt/uitest/it-tests.jar
ENV GALLERY_DIR=/opt/uitest/gallery
ENV GALLERY_PORT=3002
# ui-runtime ships Chrome-for-Testing here; Playwright drives it directly
# (no browser download, which the build network blocks).
ENV PLAYWRIGHT_CHROME=/opt/chrome-linux64/chrome

RUN set -e && bash run-it.sh && date > /opt/uitest/it-passed

# ===========================================================================
# Stage 3: final — ship only the release libs (and gallery assets).
# The COPY from uitest forces this stage to depend on a green UI test run.
# ===========================================================================
FROM ${DOCKER_URL}/${DOCKER_ORG}/web-img:latest AS final

ARG BUILD_ID

# Only exists when the UI tests passed; makes stage 3 depend on stage 2.
COPY --from=uitest /opt/uitest/it-passed /tmp/.it-passed

USER build

RUN mkdir -p /dist/release-libs /dist/s3
COPY --from=build --chown=build:build /dist/release-libs /dist/release-libs
COPY --from=build --chown=build:build /dist/s3 /dist/s3

RUN ls -la /dist/release-libs
