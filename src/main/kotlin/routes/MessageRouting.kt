package com.example.routes

import com.example.service.MediaConverter
import com.example.service.MessageService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.ktor.ext.inject

fun Application.configureMessageRouting()
{
    val messageService by inject<MessageService>()
    val mediaConverter by inject<MediaConverter>()

    routing {

        get("/messages/{page}"){

            call.pathParameters["page"]?.let {
                val offset = messageService.getLimit()
                val messagesAll = messageService.findAll(offset.times(it.toInt()))

                call.respond(Json.encodeToJsonElement(messagesAll))
            }
            call.respond(HttpStatusCode.BadRequest,"Invalid request")
        }

        get("/messages-type/{page}"){
            val queryMap = checkRequestParameters(call,"chatId","type","page") ?: return@get
            val type = queryMap["type"] as String

            if(type !in listOf("image/png","image/jpeg","video/mp4","text/plain") )
            {
                call.respond(HttpStatusCode.BadRequest,"Invalid content type")
                return@get
            }

            val offset = messageService.getLimit().times(queryMap["page"] as Int)
            val messageWithContentType = messageService.findByContentType(queryMap["chatId"] as String,type,offset.toLong())
            call.respond(Json.encodeToJsonElement(messageWithContentType))
        }
        get("/messages-chat/{page}"){

            val queryMap = checkRequestParameters(call,"chatId","page") ?: return@get
            val offset = messageService.getLimit().times(queryMap["page"] as Int)
            val messageInChat = messageService.findByChat(queryMap["chatId"] as String, offset.toLong())
            call.respond(Json.encodeToJsonElement(messageInChat))
        }

        get("/messages-user/{page}"){

            val queryMap = checkRequestParameters(call,"chatId","userId","page") ?: return@get
            val offset = messageService.getLimit().times(queryMap["page"] as Int)
            val messageByUser = messageService.findByUser(queryMap["userId"] as String, queryMap["chatId"] as String, offset.toLong())
            call.respond(Json.encodeToJsonElement(messageByUser))
        }

        get("/take-media")
        {
            call.queryParameters["path"]?.let {
                mediaConverter.checkMediaContent(it)
            }?.let {
                call.respond(mapOf("mediaContent" to it))
            }
        }
    }
}

suspend fun checkRequestParameters(call: ApplicationCall, vararg args:String ): MutableMap<String,Any>?
{
    val resMap = mutableMapOf<String,Any>()
    args.forEach{
        if (it == "page")
        {
            val page = call.parameters["page"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, "$it must be a valid integer")
                return null
            }
            resMap[it] = page
        }
        else{
            val paramValue = call.request.queryParameters[it] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid $it id. Value cannot be null")
                return null
            }
            resMap[it] =  paramValue
        }

    }
    return resMap
}
