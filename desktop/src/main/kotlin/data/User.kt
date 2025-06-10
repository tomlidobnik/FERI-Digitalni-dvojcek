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

data class PublicUser(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "username") val username: String,
    @Json(name = "first_name") val firstname: String,
    @Json(name = "last_name") val lastname: String,
    @Json(name = "email") val email: String
)
