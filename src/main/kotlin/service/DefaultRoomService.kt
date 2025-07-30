package com.example.service

import com.example.model.Chat
import com.example.model.ChatResponse
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class DefaultRoomService: RoomService {

    override suspend fun create(name: String): UUID = dbQuery{
        Chat.insert{
            it[id] = UUID.randomUUID()
            it[this.name] = name
        }[Chat.id]
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery{
        Chat.deleteWhere{Chat.id.eq(id)} > 0
    }

    override suspend fun update(id: UUID, name: String) = dbQuery {
        Chat.update{
            it[this.name] = name
        }
        return@dbQuery
    }

    override suspend fun findAll(): List<ChatResponse> = dbQuery{
        Chat.selectAll().map { ChatResponse(it[Chat.id].toString(),it[Chat.name]) }
    }

    override suspend fun findById(id: UUID): ChatResponse? = dbQuery {
        Chat.selectAll().where(Chat.id eq id)
            .map { ChatResponse(it[Chat.id].toString(),it[Chat.name])}
            .singleOrNull()
    }

    suspend fun <T> dbQuery (block: suspend () ->T): T = newSuspendedTransaction(Dispatchers.IO){ block() }

}