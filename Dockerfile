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
RUN set -e &&\
    root=$PWD &&\
    echo "Building b${BUILD_ID}" &&\
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
      echo "Moving to $i" &&\
      cd modules/$i &&\
      echo "Updating libs" &&\
      bb -i '(let [build-id "'${BUILD_ID}'" \
                   group-id "'${ARTIFACT_ORG}'" \
                   project-name "'${PROJECT_NAME}'" \
                   lib (symbol (str "edd/" project-name)) \
                   new-lib (symbol (str group-id "/" project-name)) \
                   deps (read-string \
                         (slurp (io/file "deps.edn"))) \
                   global (get-in deps [:deps lib]) \
                   deps (if global \
                          (-> deps \
                            (update-in [:deps] #(dissoc % lib))  \
                            (assoc-in [:deps new-lib] {:mvn/version (str "1." build-id)})) \
                       deps) \
                   aliases [:test] \
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
                                  (clojure.pprint/pprint deps))))' &&\
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
