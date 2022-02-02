# MemopryHole Patreon Scraper
This application is designed for downloading content posted by creators on patreon.com.

# Build instructions

## Prerequisites

- Install Gradle: https://gradle.org/install/ 
  - alternatively you can use an IDE like IntelliJ with built-in Gradle Support

## Build a runnable Jar:

`gradlew build shadowJar` or `./gradlew.bat build shadowJar` will build to `build/libs/Scraper-VERSION-all.jar` (**-all** includes dependencies, the other one doesn't)

# Api Usage
## Get the subscriptions form a sessionId
HTTP **GET**: `/api/v1/lookup/patreon`

### Form Parameters:
- **sessionId**: the session id
  - Example: `LYRSF9d52ioitj7suQA78bG6z4v4b5PDPwuRW9jL62X`

> Returns a list of ids with the creator name as json

## Enqueue a Creator
HTTP **POST**: `/api/v1/enqueue/patreon`

### Form Parameters:
- **sessionId**: the session id
  - Example: `LYRSF9d52ioitj7suQA78bG6z4v4b5PDPwuRW9jL62X`
- **toImport**: the ids to import, comma separated
  - Example: `1414324,2455235,235235` or `2424242`

> Returns the `importId`

# Config

Place the `config.yml` file next to the .jar file or use environement variables.

## YAML Example
```yaml
threads: 10
minio:
  host: https://storage.memoryhole.cc
  accessKey: key
  secretKey: key
  bucketName: patreon_data
rest:
  port: 7000
```

## Environment Variables Example
`THREADS=10`<br>
`MINIO_HOST=https://storage.memoryhole.cc`<br>
`MINIO_ACCESSKEY=key`<br>
`MINIO_SECRETKEY=key`<br>
`MINIO_BUCKETNAME=patreon_data`<br>
`REST_PORT=7000`<br>
