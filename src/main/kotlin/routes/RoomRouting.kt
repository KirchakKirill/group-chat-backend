package com.example.routes

import com.example.model.ChatManager
import com.example.model.RoomChat
import com.example.model.UsersChatsRolesDTORequest
import com.example.service.RoomService
import com.example.service.UsersChatsRolesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRoomRouting()
{
    val roomService by inject<RoomService>()
    val usersChatsRolesService by inject<UsersChatsRolesService>()
    val chatManager by inject<ChatManager>()

    routing {
        authenticate("auth-jwt") {

            post("/create-chat"){
                val chatName = call.queryParameters["chatName"]
                val userId = call.queryParameters["userId"]
                if ((chatName == null || chatName.isEmpty()) || (userId==null || userId.isEmpty()) ) {
                    call.respond(HttpStatusCode.BadRequest, "URL parameter roomName and userId cannot be null")
                    return@post
                }
                else{
                    val idRoom = roomService.create(chatName)
                    usersChatsRolesService.create(UsersChatsRolesDTORequest(userId,idRoom.toString(),"ADMIN"))
                    chatManager.addRoom(RoomChat(idRoom))
                    call.respond(mapOf("RoomId" to idRoom.toString()))
                }

            }


            post("/follow_chat/{roomId}"){
                val chatId = call.pathParameters["roomId"] ?: run {
                    call.respond("Path parameter \"roomId\" cannot be null")
                    return@post
                }

                val userId = call.queryParameters["userId"] ?: run{
                    call.respond("URL parameter \"userId\" cannot be null")
                    return@post
                }

                usersChatsRolesService.create(UsersChatsRolesDTORequest(userId,chatId,"USER"))
                call.respond(HttpStatusCode.OK)
            }


        }
    }
}