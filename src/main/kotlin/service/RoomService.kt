package com.example.service

import com.example.model.Chat
import com.example.model.ChatResponse
import java.util.UUID

interface RoomService
{
    suspend fun create(name:String): UUID
    suspend fun delete(id:UUID): Boolean
    suspend fun update(id: UUID,name:String)
    suspend fun findAll():List<ChatResponse>
    suspend fun findById(id: UUID): ChatResponse?
}