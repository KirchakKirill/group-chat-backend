
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.netty)
    implementation(libs.liquibase.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    implementation(libs.nimbus.jose)
    implementation(libs.kotlin.postgresql)
    implementation(libs.kotlin.dotenv)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.hikariCP)
    implementation(libs.ktor.server.auth)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
