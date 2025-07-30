package com.example.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*


@Serializable
data class User(
    val sub: String,
    val email: String,
)



object Users : Table() {
    val googleSub  = text("google_sub")
    val email = text("email")
    override val primaryKey = PrimaryKey(googleSub)
}