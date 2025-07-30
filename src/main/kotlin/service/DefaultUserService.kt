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
    override suspend fun create(user: User): String = dbQuery {
        Users.insert {
            it[googleSub] = user.sub
            it[email] = user.email
        }[Users.googleSub]
    }

    override suspend fun findById(sub:String): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.googleSub eq sub }
                .map {
                    User(
                        it[Users.googleSub],
                        it[Users.email]
                    )
                }
                .singleOrNull()
        }
    }

    override suspend fun existsByGoogleSub(sub: String): Boolean = dbQuery {
         Users.selectAll()
            .where { Users.googleSub eq sub }
             .count()==1L
    }

    override suspend fun update(sub: String, user: User) {
        dbQuery {
            Users.update({ Users.googleSub eq sub }) {
                it[email] = user.email
            }
        }
    }

    override suspend fun delete(sub: String) {
        dbQuery {
            Users.deleteWhere { Users.googleSub.eq(sub) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}