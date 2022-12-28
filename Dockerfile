ARG DOCKER_URL
ARG DOCKER_ORG

FROM ${DOCKER_URL}/${DOCKER_ORG}/common-img:latest

ARG ARTIFACT_ORG
ENV PROJECT_NAME edd-core-web

# Custom build here
COPY src src
COPY test test
COPY deps.edn deps.edn

RUN set -e && clojure -A:test:unit

ARG BUILD_ID

COPY --chown=build:build modules modules
COPY --chown=build:build templates templates

RUN set -e &&\
    echo "Building b${BUILD_ID}" &&\
    root=$PWD &&\
    module_names="$(ls modules)" &&\
    modules=$(find modules/  -name "deps.edn" -exec dirname {} \;) &&\
    modules="$modules templates/project" &&\
    libs="$(ls -xm modules), ${PROJECT_NAME}" &&\
    for i in $modules; do \
      echo "Moving to $i" &&\
      cd $i &&\
      echo "Updating libs: $libs" &&\
      bb -i '(doseq [lib-name (clojure.string/split "'"$(echo $libs)"'" #", ")] \
              (let [build-id "'${BUILD_ID}'" \
                    group-id "'${ARTIFACT_ORG}'" \
                    lib (symbol (str "edd/" lib-name)) \
                    new-lib (symbol (str group-id "/" lib-name)) \
                    deps (read-string \
                          (slurp (io/file "deps.edn"))) \
                    global (get-in deps [:deps lib]) \
                    deps (if global \
                           (-> deps \
                             (update-in [:deps] #(dissoc % lib))  \
                             (assoc-in [:deps new-lib] {:mvn/version (str "1." build-id)})) \
                        deps) \
                    aliases (keys (:aliases deps)) \
		    deps (update deps :aliases #(dissoc % :local-dev)) \
                    deps (reduce \
                            (fn [p alias] \
                             (if (get-in p [:aliases alias :extra-deps lib]) \
                               (-> p \
                                  (update-in [:aliases alias :extra-deps] #(dissoc % lib))  \
                                  (assoc-in [:aliases alias :extra-deps new-lib] \
                                            {:mvn/version (str "1." build-id)})) \
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
    set -e && clj -A:jar  \
        --app-group-id ${ARTIFACT_ORG} \
        --app-artifact-id ${PROJECT_NAME} \
        --app-version "1.${BUILD_ID}" &&\
    cp pom.xml /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar.pom.xml &&\
    cp target/${PROJECT_NAME}-1.${BUILD_ID}.jar /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar &&\
    mvn install:install-file \
       -Dfile=target/${PROJECT_NAME}-1.${BUILD_ID}.jar \
       -DgroupId=${ARTIFACT_ORG} \
       -DartifactId=${PROJECT_NAME} \
       -DpomFile=pom.xml \
       -Dversion="1.${BUILD_ID}" \
       -Dpackaging=jar &&\
    echo "Building modules:" &&\
    for i in $(ls modules); do  \
      echo "Moving to modules/$i" &&\
      cd modules/$i &&\
      clj -A:jar  \
           --app-group-id ${ARTIFACT_ORG} \
           --app-artifact-id $i \
           --app-version "1.${BUILD_ID}" &&\
      cp pom.xml /dist/release-libs/$i-1.${BUILD_ID}.jar.pom.xml &&\
      cp target/$i-1.${BUILD_ID}.jar /dist/release-libs/$i-1.${BUILD_ID}.jar &&\
      cd $root || exit 1; \
    done


RUN ls -la target


RUN cp pom.xml /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar.pom.xml
RUN cp target/${PROJECT_NAME}-1.${BUILD_ID}.jar /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar

RUN cat pom.xml
