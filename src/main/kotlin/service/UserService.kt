package com.example.service

import com.example.model.User

interface UserService
{
    suspend fun create(user: User): String
    suspend fun update(sub:String,user:User)
    suspend fun findById(sub:String): User?
    suspend fun existsByGoogleSub(sub:String): Boolean
    suspend fun delete(sub:String)
}