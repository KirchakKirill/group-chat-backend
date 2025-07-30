package com.example.service

import com.example.model.MessageRequest
import com.example.model.MessageResponse
import com.example.model.Messages
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.slf4j.LoggerFactory
import java.lang.RuntimeException


class DefaultMessageService: MessageService {

    companion object{
        private val logger = LoggerFactory.getLogger("DefaultMessageService")
        fun getInvalidChatIdError(errorMessage:String) = "[ERROR] DefaultMessageService: problem with cast string chat id to uuid format in method $errorMessage"
        const val LIMIT = 50

    }

    override fun getLimit():Int = LIMIT

    override suspend fun create(dto: MessageRequest): Long = dbQuery {

        try {
            logger.debug("[CREATE] Creating message for chat ${dto.chatId} by user ${dto.senderId}")
            Messages.insert {
                it[chatId] = UUID.fromString(dto.chatId)
                it[senderId] = dto.senderId
                it[contentType] = dto.contentType
                it[content] = dto.content
                it[mediaContent] = dto.mediaContent
            } [Messages.id]
        }
        catch (e: Exception)
        {
            logger.info("Message error during CREATE new message: ${e.message}")
            throw RuntimeException(e.message)
        }

    }

    override suspend fun delete(id: Long): Boolean  = dbQuery{
        logger.debug("[DELETE] Deleting message for chat with id = $id")
        Messages.deleteWhere {Messages.id eq id} == 1
    }

    override suspend fun findAll(offset: Int): List<MessageResponse>  = dbQuery{
        logger.debug("[READ] Find $LIMIT messages with offset = $offset")
        Messages
            .selectAll()
            .limit(LIMIT)
            .offset(offset.toLong())
            .map { mapToMessageResponse(it) }
    }

    override suspend fun findByContentType(
        chatId: String,
        type: String,
        offset:Long
    ): List<MessageResponse> = dbQuery {
        logger.debug("[READ] Find $LIMIT messages with type  = $type in chat with id = $chatId ")
        Messages
            .selectAll()
            .where {(Messages.chatId eq UUID.fromString(chatId)) and (Messages.contentType eq type)}
            .limit(LIMIT)
            .offset(offset)
            .map { mapToMessageResponse(it) }

    }

    override suspend fun findByChat(chatId: String,offset: Long): List<MessageResponse> = dbQuery {
        try {
            logger.debug("[READ] Find $LIMIT messages in chat with id = $chatId ")
            val uuid = UUID.fromString(chatId)
            Messages
                .selectAll()
                .where {(Messages.chatId eq uuid)}
                .limit(LIMIT)
                .offset(offset)
                .map { mapToMessageResponse(it) }
        }
        catch (e: Exception)
        {
            logger.debug(getInvalidChatIdError("findByChat"))
            throw IllegalArgumentException("Invalid chatId format.\n" +
                    "Message error: ${e.message}")
        }


    }

    override suspend fun findByUser(
        userId: String,
        chatId: String,
        offset: Long
    ): List<MessageResponse>  = dbQuery{

        try {
            logger.debug("[READ] Find $LIMIT messages  in chat with id = $chatId and messages that belong to the user with id = $userId ")
            val uuid = UUID.fromString(chatId)
            Messages
                .selectAll()
                .where {(Messages.chatId eq uuid) and (Messages.senderId eq userId)}
                .limit(LIMIT)
                .offset(offset)
                .map { mapToMessageResponse(it) }
        }
        catch (e: Exception)
        {
            logger.debug(getInvalidChatIdError("findByUser"))
            throw IllegalArgumentException("Invalid chatId format.\n" +
                    "Message error: ${e.message}")
        }

    }

    override suspend fun findIdLastMessage(): Long? = dbQuery {
        Messages.select(Messages.id)
            .map { it[Messages.id] }
            .takeIf { !it.isEmpty() }
            ?.last()
    }

    suspend fun <T> dbQuery(block: suspend ()->T):T = newSuspendedTransaction(Dispatchers.IO) {block()}

    fun mapToMessageResponse(row:ResultRow): MessageResponse
    {
        return MessageResponse(
            row[Messages.id],
            row[Messages.chatId].toString(),
            row[Messages.senderId],
            row[Messages.contentType],
            row[Messages.content],
            row[Messages.mediaContent]
        )
    }

}