package com.example

import com.example.plugins.configureJWTAuthentication
import com.example.routes.configureAuthenticationRouting
import com.example.routes.configureRouting
import com.example.service.DefaultUserService
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)

}


fun Application.module() {


    val applicationHttpClient = HttpClient(CIO){

        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })

        }
    }

    val dotenv = dotenv {
        directory = "./"
        ignoreIfMissing = true
    }

    val defaultUserService = DefaultUserService()

    configureSerialization()
    configureDatabases()
    configureJWTAuthentication(applicationHttpClient,dotenv)
    configureAuthenticationRouting(httpClient = applicationHttpClient,
        dotenv = dotenv,
        userService = defaultUserService)
    configureRouting()
}


