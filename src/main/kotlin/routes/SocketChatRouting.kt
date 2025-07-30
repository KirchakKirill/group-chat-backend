package com.example.routes

import com.example.model.ChatManager
import com.example.model.ContentReceive
import com.example.model.MessageRequest
import com.example.service.MediaConverter
import com.example.service.MessageService
import com.example.service.UsersChatsRolesService
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.util.decodeBase64Bytes
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID


fun Application.configureSocketChatRouting(){

    val messageService by inject<MessageService>()
    val mediaConverter by inject<MediaConverter>()
    val usersChatsRolesService by inject<UsersChatsRolesService>()
    val chatManager  by inject<ChatManager>()

    routing {
        authenticate("auth-jwt")
        {
            webSocket("/ws_send/{chatId}") {
                val logger = LoggerFactory.getLogger("Application")
                val principal = call.principal<JWTPrincipal>()
                val userSub = principal?.payload?.subject ?: run {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid user"))
                    return@webSocket
                }
                val chatId = UUID.fromString(call.parameters["chatId"])
                val session = SocketUserSession(userSub,this)


                val checkFollowChat = usersChatsRolesService.checkUserByChat(userSub,chatId)

                if (checkFollowChat)
                {
                    val manager = chatManager[chatId] ?: run{
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT,"Incorrect roomId or equals null"))
                        return@webSocket
                    }
                    manager.join(userSub,session)

                    runCatching {
                        incoming.consumeAsFlow().collect {

                            val receiveDataFormatString = (it as Frame.Text).readText()
                            val receiveDataJson = Json.decodeFromString<ContentReceive>(receiveDataFormatString)
                            val mediaContentPath = receiveDataJson.mediaContent?.let { media ->
                                mediaConverter.saveMediaToFile(media.decodeBase64Bytes()) ?: run {
                                    close(
                                        CloseReason(
                                            CloseReason.Codes.INTERNAL_ERROR,
                                            "Failed to save media data to file"
                                        )
                                    )
                                    return@collect
                                }
                            }

                            val dto  = MessageRequest(
                                chatId = chatId.toString(),
                                senderId = userSub,
                                contentType = receiveDataJson.contentType,
                                content = receiveDataJson.content,
                                mediaContent = receiveDataJson.mediaContent
                            )
                            val id = messageService.create(dto)
                            logger.info("ID message new= $id")
                            val dtoJson = Json.encodeToString(dto)
                            manager.broadcast(dtoJson,userSub)
                            manager.bufferingMessages(dtoJson)

                        }
                    }
                        .also {
                            manager.leave(userSub)
                        }
                        .onFailure { exception ->
                            logger.info("WebSocket exception: ${exception.localizedMessage}")
                            manager.leave(userSub)

                        }
                }

            }
        }

    }
}
