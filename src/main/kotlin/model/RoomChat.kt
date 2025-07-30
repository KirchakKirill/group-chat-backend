package com.example.model

import com.example.routes.SocketUserSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlinx.serialization.json.Json

class RoomChat(val idRoom:UUID) {

    private val recipients = mutableMapOf<String, SocketUserSession>()
    private val mutex = Mutex()
    private val BUFFER_SIZE = 10
    private val bufferedMessages = mutableListOf<String>()
    private val logger = LoggerFactory.getLogger("RoomChat")

    suspend fun join(userSub: String, recipient: SocketUserSession) {
        mutex.withLock {
            recipients[userSub] = recipient
            bufferedMessages.takeLast(BUFFER_SIZE).forEach {
                try {
                    recipient.session.send(Frame.Text(it))
                }
                catch (e:Exception)
                {
                    logger.info("Error during send buffering messages: ${e.message}")
                    leave(userSub)
                }
            }
        }

        broadcastSystemMessage("$userSub присоединился")
    }

    suspend fun bufferingMessages(messageDto: String){
        mutex.withLock {
            bufferedMessages.add(messageDto)
        }
    }

    suspend fun leave(userSub: String) {
        val removed = mutex.withLock {
            recipients.remove(userSub)
        }
        removed?.let {
            try {
                it.session.close(CloseReason(CloseReason.Codes.NORMAL, "User left"))
            } catch (e: Exception) {
                logger.info("Error during closing session: ${e.message}")
            }
        }
        broadcastSystemMessage("$userSub вышел")
    }

    suspend fun broadcast(message: String, sender: String? = null) {
        val recipientsToSend = mutex.withLock {
            recipients.filterValues { sender != it.userSub }
        }
        logger.info("BROADCAST:$recipientsToSend")

        recipientsToSend.values.forEach { recipient ->
            try {
                logger.info("RECIPIENT:$recipient")
                recipient.session.send(Frame.Text(message))
                logger.info("FRAME:${Frame.Text(message)}")
            } catch (e: Exception) {
                logger.info("ERROR BROADCAST")
                leave(recipient.userSub)
            }
        }
    }

    private suspend fun broadcastSystemMessage(message: String) {
        val allRecipients = mutex.withLock { recipients.values.toList() }
        logger.info("BROADCASTSYSTEM:$allRecipients")
        allRecipients.forEach { recipient ->
            try {
                logger.info("RECIPIENT1:$recipient")
                recipient.session.send(Frame.Text("[System] $message"))
                logger.info("FRAME1:${Frame.Text("[SYSTEM]$message")}")
            } catch (e: Exception) {
                logger.info("ERROR 1 BROADCAST")
                leave(recipient.userSub)
            }
        }
    }
}