package data

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val username: String,
    @Json(name = "first_name") val firstname: String,
    @Json(name = "last_name") val lastname: String,
    val email: String,
    val password: String
)
@Serializable
data class PublicUser(
    val username: String,
    @Json(name = "first_name") val firstname: String,
    @Json(name = "last_name") val lastname: String,
    val email: String
)
