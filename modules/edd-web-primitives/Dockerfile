ARG DOCKER_URL
ARG DOCKER_ORG
ARG BUILD_ID
ARG PROJECT_NAME=edd-web-primitives

FROM ${DOCKER_URL}/${DOCKER_ORG}/web-img:b2042

ENV PROJECT_NAME edd-web-primitives

ARG ARTIFACT_ORG
ARG BUILD_ID

# Custom build from here on

USER root
RUN npm install -g shadow-cljs karma karma-cljs-test karma-chrome-launcher karma-junit-reporter
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

RUN set -e && clojure -M:lint --lint src/main src/test
RUN set -e && clojure -M:test:runner
RUN set -e && npx shadow-cljs -A:dev compile

RUN set -e &&\
    clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.9.2"}}}' \
            -M -m cljfmt.main check src/main/ src/test

RUN ls -la /dist
RUN set -e && npx shadow-cljs -A:dev release devcards

RUN ls -la /dist
RUN cp -r resources/public/* /dist/s3/
RUN sed -i 's/version=1/version='${BUILD_ID}'/g' /dist/s3/index.html
RUN ls -la /dist

RUN set -e && clojure -A:test -Sdeps '{:deps {luchiniatwork/cambada {:mvn/version "1.0.5"}}}' \
                      -m cambada.jar \
                      --app-version "1.0.${BUILD_ID}" \
                      --app-artifact-id "${PROJECT_NAME}" \
                      --app-group-id "${ARTIFACT_ORG}" \
                      --copy-source \
                      -o /dist/release-libs/; \
                    cp pom.xml "/dist/release-libs/${PROJECT_NAME}-1.0.${BUILD_ID}.jar.pom.xml"; \