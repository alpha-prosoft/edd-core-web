ARG DOCKER_URL
ARG DOCKER_ORG
ARG BUILD_ID

ARG PROJECT_NAME=edd-web-widgets

FROM ${DOCKER_URL}/${DOCKER_ORG}/web-devcards-ansible-img:latest

ARG ARTIFACT_ORG

RUN set -e && clj -A:jar -Sdeps '{:deps {luchiniatwork/cambada {:mvn/version "1.0.2"}}}' \
          -m cambada.jar \
          --app-version "1.${BUILD_ID}" \
          --app-artifact-id "${PROJECT_NAME}" \
          --app-group-id "${ARTIFACT_ORG}" \
          --copy-source \
          -o /dist/release-libs/; \
      cp pom.xml "/dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar.pom.xml"; \
