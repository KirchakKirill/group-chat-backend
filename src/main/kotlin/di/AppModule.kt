package com.example.di

import com.example.model.ChatManager
import org.koin.dsl.module
import com.example.service.DefaultMessageService
import com.example.service.DefaultRoomService
import com.example.service.DefaultUserService
import com.example.service.DefaultUsersChatsRolesService
import com.example.service.IdGenerator
import com.example.service.MediaConverter
import com.example.service.MessageService
import com.example.service.RoomService
import com.example.service.UserService
import com.example.service.UsersChatsRolesService
import kotlinx.coroutines.runBlocking

val appModule = module{

    single<MessageService> { DefaultMessageService() }
    single { IdGenerator(get()).apply {
        runBlocking {
            initialize()
        }
    } }
    single { MediaConverter(get(),"uploads") }
    single<UsersChatsRolesService> { DefaultUsersChatsRolesService() }
    single<RoomService> { DefaultRoomService() }
    single<UserService> { DefaultUserService() }
    single { ChatManager(get()).apply {
        runBlocking {
            initialize()
        }
    } }
}