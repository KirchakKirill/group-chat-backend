package com.example.service

import com.example.model.ChatResponse
import com.example.model.User
import com.example.model.UsersChatsRolesDTORequest
import java.util.UUID

interface UsersChatsRolesService
{
    suspend fun create(dto: UsersChatsRolesDTORequest)
    suspend fun delete(userId:String,chatId:String): Boolean
    suspend fun findAllUsersByChat(chatId:String):List<User>
    suspend fun findAllChatsByUser(userSub:String):List<ChatResponse>
    suspend fun checkUserByChat(userId:String,chatId: UUID): Boolean
}