package com.example.config


import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.AESDecrypter
import com.nimbusds.jose.crypto.AESEncrypter

import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet

import io.github.cdimascio.dotenv.Dotenv

import java.util.Date
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.JWSAlgorithm.HS256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory


class JWTConfig(dotenv: Dotenv)
{
    val jwtSign = SecretKeySpec(
        dotenv.get("JWT_SIGN").toByteArray(Charsets.UTF_8),
        "HmacSHA256"
    )
    val jwtEncrypt = SecretKeySpec(
        dotenv.get("JWT_ENCRYPT").toByteArray(Charsets.UTF_8).copyOf(32),
        "AES"
    )


    val jwtIssuer = dotenv.get("JWT_ISSUER")

    val jwtAudience = dotenv.get("JWT_AUDIENCE").split("\\S+")


    val jwtRealm = dotenv.get("JWT_REALM")

    val logger = LoggerFactory.getLogger("Application")

    fun createJWTToken(googleToken:String, expireAt:Date,sub:String):String
    {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(HS256)
                .keyID(UUID.randomUUID().toString())
                .build(),
            JWTClaimsSet.Builder()
                .subject(sub)
                .issuer(jwtIssuer)
                .audience(jwtAudience)
                .expirationTime(expireAt)
                .claim("google_token", googleToken)
                .build()

        )
        signedJWT.sign(MACSigner(jwtSign))

        logger.info("JWS creating: ${signedJWT.serialize()}")

        val jweHeader = JWEHeader.Builder(JWEAlgorithm.A256GCMKW, EncryptionMethod.A256GCM)
            .contentType("JWS")
            .build()

        val jweObject = JWEObject(jweHeader, Payload(signedJWT.serialize()))
        jweObject.encrypt(AESEncrypter(jwtEncrypt))
        logger.info("JWE creating: ${jweObject.serialize()}")
        return jweObject.serialize()
    }

    fun decryptJWTToken(encryptedJWTToken: String): EncryptedJWT?{
        return try {
            val jwe = EncryptedJWT.parse(encryptedJWTToken)
            jwe.decrypt(AESDecrypter(jwtEncrypt))
            jwe
        } catch (e: Exception) {
            null
        }
    }

    fun createJWEVerifier(decrypt: (String)-> EncryptedJWT?): JWTVerifier
    {

        return object: JWTVerifier
        {
            override fun verify(token: String?): DecodedJWT? {
                requireNotNull(token) { "Token cannot be null" }
                val jwe = try {
                    decrypt(token)
                } catch (e: Exception) {
                    throw JWTVerificationException("Token decryption failed", e)
                }
                requireNotNull(jwe) {"JWE cannot be null"}
                logger.info("JWE token: ${jwe.serialize()}")

                val signedJWT = SignedJWT.parse(jwe.payload.toString())
                require(signedJWT.verify(MACVerifier(jwtSign))) {"Invalid sign"}

                logger.info("JWS token: ${signedJWT.serialize()}")

                val claims = signedJWT.jwtClaimsSet
                with(claims) {
                    require(issuer == jwtIssuer) { "Invalid issuer" }
                    logger.info((issuer==jwtIssuer).toString())
                    require(expirationTime?.after(Date()) == true) { "Token expired" }
                    logger.info((expirationTime?.after(Date()) == true).toString())
                    require(audience.filter { a -> jwtAudience.contains(a)}.size == jwtAudience.size) { "Invalid audience" }
                    logger.info("Expected audience: $jwtAudience, token audience: ${claims.audience}")
                    logger.info("Audience check: "+ (audience.filter { a -> jwtAudience.contains(a)}.size == jwtAudience.size))
                }

                logger.info("""
    JWT Config:
    - Issuer: $jwtIssuer
    - Audience: $jwtAudience
""".trimIndent())

                logger.info("""
    JWT Config:
    - Issuer: ${claims.issuer}
    - Audience: ${claims.audience}
""".trimIndent())

                logger.info("I`m here 113")
                testAdapter(signedJWT,token)
                return JWSToDecodedJWTAdapter(token,signedJWT)


            }

            override fun verify(jwt: DecodedJWT?): DecodedJWT?  = throw UnsupportedOperationException()
        }
    }

    fun testAdapter(signedJWT: SignedJWT,token:String) {
        val adapter = JWSToDecodedJWTAdapter(token,signedJWT)

        logger.info("Algorithm: ${adapter.algorithm}")
        logger.info("Signature: ${adapter.signature}")
        logger.info("ExpiresAt: ${adapter.expiresAt}")
        logger.info("Id: ${adapter.id}")
        logger.info("Token: ${adapter.token}")
        logger.info("Audience: ${adapter.audience}" )
        val claims  = adapter.claims.map{(k,v)-> "$k:${v.asString()}"}
        logger.info("Claims: $claims")
        logger.info("Issuer: ${adapter.issuer}" )
        logger.info("Subject: ${adapter.subject}")
        logger.info("Header: ${adapter.header}" )
        logger.info("expiresAtAsInstant: ${adapter.expiresAtAsInstant}" )
        logger.info("IssueAt: ${adapter.issuedAt}" )
        logger.info("NotBefore: ${adapter.notBefore}" )
        logger.info("NotBeforeInstant: ${adapter.notBeforeAsInstant}" )
        logger.info("ContentType: ${adapter.contentType}" )
        logger.info("Payload: ${adapter.payload}" )
        logger.info("Type: ${adapter.type}" )
        logger.info("KeyID: ${adapter.keyId}" )



    }

}