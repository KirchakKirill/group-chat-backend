package com.example.routes

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)

    routing {
        authenticate("auth-jwt") {
            get("/") {
                call.respondText("Hello World!")
            }
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
    }
}
@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
