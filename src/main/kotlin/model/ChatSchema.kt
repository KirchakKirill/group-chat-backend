package com.example.model

import org.jetbrains.exposed.sql.Table
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(val id: String, val name:String)

object Chat: Table("chats")
{
    val id = uuid("id")
    val name  = text("name")
    override val primaryKey = PrimaryKey(id)
}