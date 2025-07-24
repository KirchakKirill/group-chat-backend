package com.example.plugins


import com.example.config.JWTConfig
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.http.auth.HttpAuthHeader

import org.slf4j.LoggerFactory
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.server.response.respond


fun Application.configureJWTAuthentication(httpClient: HttpClient, dotenv: Dotenv)
{
    val logger = LoggerFactory.getLogger("Application")
    val configJWT = JWTConfig(dotenv)
    install(Authentication)
    {
        jwt("auth-jwt") {
            realm = configJWT.jwtRealm

            verifier { authHeader ->
                val token = when (authHeader) {
                    is HttpAuthHeader.Single -> {
                        if (authHeader.authScheme == "Bearer") {
                            authHeader.blob
                        } else {
                            throw JWTVerificationException("Invalid auth scheme: expected 'Bearer'")
                        }
                    }
                    else -> throw JWTVerificationException("Invalid Authorization header format")
                }

                configJWT.createJWEVerifier { _ -> configJWT.decryptJWTToken(token) }
            }

            validate {  credentials ->
                try {
                    logger.info("=== Validate block started ===")
                    logger.info("Payload: ${credentials.payload}")
                    JWTPrincipal(credentials.payload)
                } catch (e: Exception) {
                    logger.error("Validate block failed", e)
                    null
                }
            }

            challenge { defaultSchema, realm ->
                val authHeader =  call.request.headers["Authorization"]

                when{
                    authHeader == null -> call.respond(HttpStatusCode.Unauthorized, "Missing token")
                    !authHeader.startsWith("Bearer") -> call.respond(HttpStatusCode.Unauthorized, "Invalid schema")
                    else -> call.respond(HttpStatusCode.Unauthorized, "Failed authentication")
                }

            }

        }
        configureGoogleOAuth(httpClient,dotenv)
    }

}