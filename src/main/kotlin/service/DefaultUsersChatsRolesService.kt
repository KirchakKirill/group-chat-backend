package com.example.service

import com.example.model.Chat
import com.example.model.ChatResponse
import com.example.model.User
import com.example.model.Users
import com.example.model.UsersChatsRoles
import com.example.model.UsersChatsRolesDTORequest
import com.example.model.UsersChatsRolesDTOResponse
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.Uuid

class DefaultUsersChatsRolesService: UsersChatsRolesService {
    override suspend fun create(dto: UsersChatsRolesDTORequest) = dbQuery{
        UsersChatsRoles.insert {
            it[userId] = dto.userId
            it[chatId] = UUID.fromString(dto.chatId)
            it[role] = dto.role
        }
        return@dbQuery
    }

    override suspend fun delete(userId:String,chatId:String): Boolean = dbQuery {

        UsersChatsRoles.deleteWhere { (this.userId eq userId).and(this.chatId eq UUID.fromString(chatId))} > 0
    }

    override suspend fun findAllUsersByChat(chatId:String):List<User>
    {
        return dbQuery {
            (Users innerJoin UsersChatsRoles)
                .select(Users.googleSub,Users.email)
                .where(UsersChatsRoles.chatId eq UUID.fromString(chatId))
                .map { row ->
                    User(
                        row[Users.googleSub],
                        row[Users.email]
                    )
                }

        }
    }

    override suspend fun findAllChatsByUser(userSub:String):List<ChatResponse>
    {
        return dbQuery {
            (Chat innerJoin UsersChatsRoles)
                .select(Chat.id,Chat.name)
                .where(UsersChatsRoles.userId eq userSub)
                .map { row ->
                    ChatResponse(
                        row[Chat.id].toString(),
                        row[Chat.name]
                    )
                }

        }
    }
    override suspend fun checkUserByChat(userId:String,chatId: UUID): Boolean = dbQuery {
        UsersChatsRoles.select(UsersChatsRoles.id).where(
            (UsersChatsRoles.userId eq userId)
                    and
                    (UsersChatsRoles.chatId eq chatId)
        ).count() != 0L
    }


    suspend fun <T> dbQuery(block: suspend () -> T):T = newSuspendedTransaction(Dispatchers.IO){block()}
}