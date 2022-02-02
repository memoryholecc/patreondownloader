import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.apollographql.apollo") version "2.5.10"
}

group = "cc.memoryhole"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20211018.2")
    implementation("commons-io:commons-io:20030203.000550")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("io.javalin:javalin:4.1.1")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("io.minio:minio:8.3.1")
    implementation("com.uchuhimo:konf-yaml:1.1.2")
    // The core runtime dependencies
    implementation("com.apollographql.apollo:apollo-runtime:2.5.10")
    // Coroutines extensions for easier asynchronicity handling
    implementation("com.apollographql.apollo:apollo-coroutines-support:2.5.10")
    //implementation("io.github.microutils:kotlin-logging:1.5.9")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.andreapivetta.kolor:kolor:1.0.0")

}
// https://www.apollographql.com/docs/android/essentials/get-started-kotlin/
/**
 * Download the schema:
 * ./gradlew downloadApolloSchema --endpoint="http://localhost:8080/graphql" --schema="src/main/graphql/cc/memoryhole/scraper/schema.graphqls"
 */
apollo {
    packageName.set("cc.memoryhole.scraper.graphql")
    // instruct the compiler to generate Kotlin models
    generateKotlinModels.set(true)
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "cc.memoryhole.scraper.ApplicationKt"
    }
}