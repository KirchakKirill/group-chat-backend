package com.example.plugins


import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.configureWebSocket()
{
    install(WebSockets)
    {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout=  30.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

}
