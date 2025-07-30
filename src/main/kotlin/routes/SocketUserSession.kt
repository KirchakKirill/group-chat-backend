package com.example.routes

import io.ktor.server.websocket.DefaultWebSocketServerSession

data class SocketUserSession(val userSub: String, val session: DefaultWebSocketServerSession)