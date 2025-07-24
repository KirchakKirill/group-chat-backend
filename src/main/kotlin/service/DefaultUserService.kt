package com.example.service

import com.example.model.User
import com.example.model.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class DefaultUserService: UserService
{
    override suspend fun create(user: User): Int = dbQuery {
        Users.insert {
            it[googleSub] = user.sub
            it[email] = user.email
        }[Users.id]
    }

    override suspend fun findById(id: Int): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.id eq id }
                .map {
                    User(
                        it[Users.googleSub],
                        it[Users.email]
                    )
                }
                .singleOrNull()
        }
    }

    override suspend fun findByGoogleSub(sub: String): Boolean = dbQuery {
         Users.selectAll()
            .where { Users.googleSub eq sub }
             .count()==1L
    }

    override suspend fun update(id: Int, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[googleSub] = user.sub
                it[email] = user.email
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}