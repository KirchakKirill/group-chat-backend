package com.example.plugins

import com.example.model.User
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun AuthenticationConfig.configureGoogleOAuth(httpClient: HttpClient, dotenv: Dotenv )
{
    oauth("google-oauth"){
        urlProvider = {"http://localhost:8080/callback"}
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name ="google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                requestMethod = HttpMethod.Post,
                clientId = dotenv.get("GOOGLE_CLIENT_ID"),
                clientSecret = dotenv.get("GOOGLE_CLIENT_SECRET"),
                defaultScopes = listOf("email", "profile",
                    "openid"),
                extraAuthParameters = listOf("access_type" to "offline",
                    "prompt" to "consent"),

            )

        }
        client = httpClient
    }
}

@Serializable
data class GoogleErrorResponse(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String
)

@Serializable
data class GoogleSuccessResponse(
    @SerialName("sub")
    val sub: String,
    @SerialName("email")
    val email: String,
    @SerialName("name")
    val name:String,

    @SerialName("given_name")
    val givenName:String,

    @SerialName("family_name")
    val familyName:String,

    @SerialName("picture")
    val picture:String,

    @SerialName("email_verified")
    val emailVerified: Boolean

)

suspend fun fetchGoogleUserInfo(httpClient: HttpClient, accessToken: String): User? {
    val response = httpClient.get("https://openidconnect.googleapis.com/v1/userinfo") {
        parameter("access_token", accessToken)
    }
    val responseBody = response.bodyAsText()

    return when {
        responseBody.contains("error") -> {
            val error = Json.decodeFromString<GoogleErrorResponse>(responseBody)
            throw IllegalStateException("Google API error: ${error.errorDescription}")
        }
        else -> {
            val user = Json.decodeFromString<GoogleSuccessResponse>(responseBody)
            User(sub = user.sub, email = user.email)
        }
    }
}

