package com.example.routes

import com.example.model.User
import com.example.service.UsersChatsRolesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.ktor.ext.inject

fun Application.configureUsersChatsRouting()
{
    val usersChatsRolesService by inject<UsersChatsRolesService>()

    routing {

        authenticate("auth-jwt")
        {
            get("/users/{chatId}"){
                val chatId = call.pathParameters["chatId"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "ID chat cannot be null"
                    )
                    return@get
                }

                chatId.let {
                    if (it.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "ID chat cannot be empty")
                        return@get
                    }
                    else {

                        val users = usersChatsRolesService.findAllUsersByChat(it)

                        users.run {
                            if(this.isEmpty()) {
                                call.respond(HttpStatusCode.BadRequest,"Incorrect chat ID ")
                                return@get
                            }
                        }

                        val sendData:Map<String,User> = users.associateBy { it -> it.sub }

                        call.respond(Json.encodeToJsonElement(sendData))
                    }
                }


            }

            get("/chats/{userId}"){
                val userId = call.pathParameters["userId"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "ID user cannot be null"
                    )
                    return@get
                }

                userId.let {
                    if (it.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "ID user cannot be empty")
                        return@get
                    }
                    else {

                        val chats = usersChatsRolesService.findAllChatsByUser(it)

                        chats.run {
                            if(this.isEmpty()) {
                                call.respond(HttpStatusCode.BadRequest,"Incorrect user ID ")
                                return@get
                            }
                        }

                        val sendData:Map<String,String> = chats.associate { it -> it.id to it.name }

                        call.respond(Json.encodeToJsonElement(sendData))
                    }
                }


            }
        }


    }
}