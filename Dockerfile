FROM gradle:jdk11 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build shadowJar --no-daemon

ARG REST_PORT=7000
ARG MINIO_PUBLIC_ENDPOINT=https://static-1.memoryhole.cc/memoryhole-public
ARG MINIO_HOST=https://us-nyc.hypepel.space
ARG BACKEND_ENDPOINT=http://api.memoryhole.cc/graphql


FROM openjdk:11-jre-slim

ENV VERSION=1.0-SNAPSHOT \
    THREADS=10 \
    REST_PORT=${REST_PORT} \
    MINIO_HOST=${MINIO_HOST} \
    BACKEND_ENDPOINT=${BACKEND_ENDPOINT} \
    MINIO_PUBLIC_ENDPOINT=${MINIO_PUBLIC_ENDPOINT}

EXPOSE ${REST_PORT}

WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/Scraper-$VERSION-all.jar /app/scraper.jar
COPY ./docker/entry.sh /app/docker/entry.sh

CMD ["sh", "/app/docker/entry.sh"]
