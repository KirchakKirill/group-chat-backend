package com.example.service

import com.example.model.MessageRequest
import com.example.model.MessageResponse

interface MessageService
{
    suspend fun create(dto: MessageRequest): Long
    suspend fun delete(id:Long): Boolean
    suspend fun findAll(offset: Int):List<MessageResponse>
    suspend fun findByContentType(chatId:String,type:String,offset:Long):List<MessageResponse>
    suspend fun findByChat(chatId:String,offset:Long):List<MessageResponse>
    suspend fun findByUser(userId:String,chatId:String,offset:Long):List<MessageResponse>
    suspend fun findIdLastMessage(): Long?
    fun getLimit():Int
}