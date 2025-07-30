package com.example.model


import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import  kotlinx.serialization.Serializable

@Serializable
data class MessageRequest(
    val chatId: String,
    val senderId: String,
    val contentType:String,
    val content:String = "",
    val mediaContent: String?
)

@Serializable
data class MessageResponse(
    val id: Long,
    val chatId: String,
    val senderId: String,
    val contentType:String,
    val content:String = "",
    val mediaContent: String?
)

@Serializable
data class ContentReceive(
    val contentType:String,
    val content:String,
    val mediaContent: String?
)


object Messages: Table("messages")
{
    val id = long("id").autoIncrement()
    val chatId = uuid("chat_id").references(Chat.id)
    val senderId = text("sender_id").references(Users.googleSub)
    val contentType = varchar("content_type", length = 15)
        .check { Op.build { it inList listOf("image/png","image/jpeg","video/mp4","text/plain")} }
    val content = text("content")
    val mediaContent = text("media_content").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        check{
            Op.build { ((contentType eq "text/plain") and (mediaContent eq null)) or
                    ((contentType inList listOf("image/png","image/jpeg","video/mp4") and not(mediaContent eq  null)))
            }
        }
    }
}

