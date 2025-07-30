package com.example.model

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SetOperation
import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

@Serializable
data class UsersChatsRolesDTORequest(
    val userId:String,
    val chatId:String,
    val role:String
)

@Serializable
data class UsersChatsRolesDTOResponse(
    val id:Long,
    val userId:String,
    val chatId:String,
    val role:String
)

object UsersChatsRoles : Table("users_chats_roles")
{
    val id = long("id").autoIncrement()
    val userId = text("user_id").references(Users.googleSub)
    val chatId = uuid("chat_id").references(Chat.id)
    val role = varchar("role", length = 10).check {Op.build { it inList listOf("USER","ADMIN","MODERATOR")}}
    override val primaryKey = PrimaryKey(id)
}