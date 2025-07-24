package com.example.service

import com.example.model.User

interface UserService
{
    suspend fun create(user: User): Int
    suspend fun update(id:Int,user:User)
    suspend fun findById(id:Int): User?
    suspend fun findByGoogleSub(sub:String): Boolean
    suspend fun delete(id:Int)
}