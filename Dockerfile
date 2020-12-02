FROM maven:3.6.3-openjdk-8 AS build

RUN apt-get -qq update \
        && apt-get install zip \
        && apt-get clean

ENV ONTOP_HOME=/ontop

WORKDIR $ONTOP_HOME

COPY .git .git
COPY .github .github
COPY .gitignore .gitignore
COPY .gitlab-ci.yml .gitlab-ci.yml
COPY .gitmodules .gitmodules
COPY .mvn .mvn
COPY .travis.yml .travis.yml
COPY LICENSE.txt LICENSE.txt
COPY README.md README.md
COPY binding binding
COPY build build
COPY build-release.sh build-release.sh
COPY client client
COPY core core
COPY db db
COPY documentation documentation
COPY engine engine
COPY licenses licenses
COPY mapping mapping
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY ontology ontology
COPY pom.xml pom.xml
COPY test test

RUN ./build-release.sh

FROM openjdk:8-jdk-alpine

RUN apk add --no-cache bash

VOLUME /tmp

WORKDIR /opt

COPY --from=build /ontop/build/distribution/ontop-cli/ontop-cli*.zip /opt

RUN mkdir /opt/ontop

RUN unzip -o ontop-cli*.zip -d ontop

RUN rm -r ontop/ontop.bat ontop/ontop ontop/ontop-completion.sh ontop/jdbc 
RUN rm ontop-cli*.zip

COPY ./client/docker/entrypoint.sh ./ontop/entrypoint.sh
COPY ./client/docker/ontop.sh ./ontop/ontop.sh

EXPOSE 8080

WORKDIR /opt/ontop

ENTRYPOINT ./entrypoint.sh