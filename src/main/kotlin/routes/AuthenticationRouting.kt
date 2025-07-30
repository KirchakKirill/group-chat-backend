package com.example.routes

import com.example.config.JWTConfig
import com.example.plugins.fetchGoogleUserInfo
import com.example.service.UserService
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import java.util.Date
import org.koin.ktor.ext.inject


fun Application.configureAuthenticationRouting(httpClient: HttpClient,dotenv: Dotenv) {
    val userService by inject<UserService>()
    val logger = LoggerFactory.getLogger("Application")
   routing {
       authenticate("google-oauth")
       {
           get("/login"){

           }

           get("/callback")
           {
               val currentPrincipal:OAuthAccessTokenResponse.OAuth2? = call.principal()
               if (currentPrincipal == null)
               {
                   call.respond(HttpStatusCode.Unauthorized,"Failed OAuth authentication")
                   return@get
               }

               val googleToken  = currentPrincipal.accessToken

               val userInfo = fetchGoogleUserInfo(httpClient,googleToken)

               if (userInfo!=null) {
                    if (!userService.existsByGoogleSub(userInfo.sub)) userService.create(userInfo)


                   val expiresAt = Date(System.currentTimeMillis() + currentPrincipal.expiresIn * 1000L)
                   val jwtToken = JWTConfig(dotenv).createJWTToken(googleToken, expiresAt, userInfo.sub)
                   call.respond(mapOf("token" to jwtToken))
               }
               else
                   call.respond(HttpStatusCode.BadRequest,"userInfo cannot equals null")

           }

       }
   }
}


