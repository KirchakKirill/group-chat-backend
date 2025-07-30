package com.example

import com.example.di.appModule
import com.example.plugins.configureJWTAuthentication
import com.example.plugins.configureWebSocket
import com.example.routes.configureAuthenticationRouting
import com.example.routes.configureMessageRouting
import com.example.routes.configureRoomRouting
import com.example.routes.configureRouting
import com.example.routes.configureSocketChatRouting
import com.example.routes.configureUsersChatsRouting
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

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

    install(Koin){
        modules(appModule)
        slf4jLogger()
    }

    val dotenv = dotenv {
        directory = "./"
        ignoreIfMissing = true
    }



    configureSerialization()
    configureDatabases()
    configureJWTAuthentication(applicationHttpClient,dotenv)
    configureWebSocket()
    configureAuthenticationRouting(httpClient = applicationHttpClient,
        dotenv = dotenv)
    configureRouting()
    configureSocketChatRouting()
    configureUsersChatsRouting()
    configureRoomRouting()
    configureMessageRouting()
}


