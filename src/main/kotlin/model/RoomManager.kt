package com.example.model

import com.example.service.RoomService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.collections.set

class ChatManager(val chatService: RoomService)
{
    private val chats = mutableMapOf<UUID, RoomChat>()
    private val mutex = Mutex()
    private val logger  = LoggerFactory.getLogger("RoomManager")

    suspend fun initialize() {

        try {
            val chats = chatService.findAll()
            addRooms(chats.map {RoomChat(UUID.fromString(it.id))})
        }
        catch (e: Exception)
        {
            logger.info("Error during initialize "+
                    "Message error: ${e.message}")
        }
    }

    suspend fun addRoom(room: RoomChat):Unit = mutex.withLock {
        try {
            chats[room.idRoom] = room
        }
        catch (e: Exception)
        {
            logger.error("Error during adding room with id = ${room.idRoom} \n" +
                    "Message error: ${e.message}")
        }
    }


    private fun addRooms(rooms:List<RoomChat>){

        if (!rooms.isEmpty())
            rooms.forEach { this.chats[it.idRoom] = it }
                .also {
                    logger.info("Success adding to rooms map")
                }
    }

    suspend fun deleteRoom(id: UUID) = mutex.withLock {
        try {
            chats.remove(id)
        }
        catch (e: Exception)
        {
            logger.error("Error during deleting room with id = $id \n" +
                    "Message error: ${e.message}")
        }
    }

    suspend operator fun get(index: UUID): RoomChat? = mutex.withLock {
        chats[index]
    }
}
