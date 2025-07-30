
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
    implementation(libs.exposed.dao)
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
    implementation(libs.ktor.server.websockets)
    implementation(libs.kotlin.dotenv)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.hikariCP)
    implementation(libs.ktor.server.auth)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)
    implementation(libs.koin.logger.slf4j)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

}
