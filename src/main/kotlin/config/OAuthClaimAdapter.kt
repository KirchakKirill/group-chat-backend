package com.example.config

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
private val logger = LoggerFactory.getLogger("Application")
class JWSToDecodedJWTAdapter(private val token:String, private val jws: SignedJWT) : DecodedJWT {


    override fun getAlgorithm(): String = jws.header.algorithm.name
    override fun getType(): String? = jws.header.type?.type
    override fun getContentType(): String? = jws.header.contentType
    override fun getKeyId(): String? = jws.header.keyID
    override fun getHeaderClaim(name: String): Claim = NimbusClaimAdapter(jws.header.getCustomParam(name))


    override fun getIssuer(): String? = jws.jwtClaimsSet.issuer
    override fun getSubject(): String? = jws.jwtClaimsSet.subject
    override fun getAudience(): List<String>? = jws.jwtClaimsSet.audience?.toList()
    override fun getExpiresAt(): Date? = jws.jwtClaimsSet.expirationTime
    override fun getNotBefore(): Date? = jws.jwtClaimsSet.notBeforeTime
    override fun getIssuedAt(): Date? = jws.jwtClaimsSet.issueTime
    override fun getId(): String? = jws.jwtClaimsSet.jwtid

    override fun getToken(): String = token
    override fun getHeader(): String? = jws.header.toString()
    override fun getPayload(): String?  = Base64.getEncoder().encodeToString(jws.payload.toBytes())
    override fun getSignature(): String? = jws.signature.toString()

    private val claimsCache by lazy {
        jws.jwtClaimsSet.claims.entries.associate { it.key to NimbusClaimAdapter(it.value) }
    }

    override fun getClaim(name: String): Claim = claimsCache[name] ?: NimbusNullClaim
    override fun getClaims(): Map<String, Claim> = claimsCache


    private class NimbusClaimAdapter(private val value: Any?) : Claim {
        override fun isNull(): Boolean = value == null
        override fun isMissing(): Boolean = false

        override fun asBoolean(): Boolean? = when (value) {

            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value.toBooleanStrictOrNull()
            else -> null
        }

        override fun asInt(): Int? = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }

        override fun asLong(): Long? = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }

        override fun asDouble(): Double? = when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }

        override fun asString(): String? = value?.toString()
        override fun asDate(): Date? = when (value) {
            is Date -> value
            is Number -> Date(value.toLong() * 1000)
            is String -> value.toLongOrNull()?.let { Date(it * 1000) }
            else -> null
        }

        override fun asInstant(): Instant? = asDate()?.toInstant()

        override fun <T> asArray(clazz: Class<T>): Array<T?>? {
            if (value !is Array<*>) return null
            logger.info("asArray")
            return try {
                @Suppress("UNCHECKED_CAST")
                val array = java.lang.reflect.Array.newInstance(clazz, value.size) as Array<T?>
                value.forEachIndexed { i, item ->
                    array[i] = if (item != null) clazz.cast(item) else null
                }
                array
            } catch (e: Exception) {
                throw JWTDecodeException("Array conversion failed for type ${clazz.name}", e)
            }
        }


        override fun <T> asList(clazz: Class<T>): List<T>? {
            logger.info("asList")
            if (value is List<*>) {
                return try {
                    value.mapNotNull { clazz.cast(it) }
                } catch (e: ClassCastException) {
                    throw JWTDecodeException("List item type mismatch", e)
                }
            }
            return null
        }

        override fun asMap(): Map<String, Any> {
            logger.info("asMap")
            if (value is Map<*, *>) {
                return try {
                    value.entries.associate { it.key.toString() to it.value!! }
                } catch (e: Exception) {
                    throw JWTDecodeException("Map conversion failed", e)
                }
            }
            throw JWTDecodeException("Value is not a Map")
        }

        override fun <T : Any?> `as`(clazz: Class<T?>?): T? {
            logger.info("as")
            return try {
                if (clazz?.isInstance(value) == true) clazz.cast(value) else null
            } catch (e: ClassCastException) {
                throw JWTDecodeException("Type conversion failed", e)
            }
        }

    }

    private object NimbusNullClaim : Claim {
        override fun isNull(): Boolean = true
        override fun isMissing(): Boolean = true
        override fun asBoolean(): Boolean? = null
        override fun asInt(): Int? = null
        override fun asLong(): Long? = null
        override fun asDouble(): Double? = null
        override fun asString(): String? = null
        override fun asDate(): Date? = null
        override fun asInstant(): Instant? = null
        override fun <T> asArray(clazz: Class<T>): Array<T>? = null
        override fun <T> asList(clazz: Class<T>): List<T>? = null
        override fun asMap(): Map<String, Any> = throw JWTDecodeException("Claim is null")
        override fun <T:Any?> `as` (clazz: Class<T>): T? = null
    }

}