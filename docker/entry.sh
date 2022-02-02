#!/bin/bash

SECRET_ROOT=/run/secrets

if [ -f "$SECRET_ROOT/MINIO_ACCESSKEY" ]; then
    export MINIO_ACCESSKEY=$(cat $SECRET_ROOT/MINIO_ACCESSKEY)
    echo "[MINIO_ACCESSKEY=MINIO_ACCESSKEY] Using Secret"
fi

if [ -f "$SECRET_ROOT/MINIO_SECRETKEY" ]; then
    export MINIO_SECRETKEY=$(cat $SECRET_ROOT/MINIO_SECRETKEY)
    echo "[MINIO_SECRETKEY=MINIO_SECRETKEY] Using Secret"
fi

if [ -f "$SECRET_ROOT/MINIO_HOST" ]; then
    export MINIO_HOST=$(cat $SECRET_ROOT/MINIO_HOST)
    echo "[MINIO_HOST=MINIO_HOST] Using Secret"
fi

if [ -f "$SECRET_ROOT/API_KEY" ]; then
    export BACKEND_APIKEY=$(cat $SECRET_ROOT/API_KEY)
    echo "[API_KEY=BACKEND_APIKEY] Using Secret"
fi

java -jar scraper.jar
