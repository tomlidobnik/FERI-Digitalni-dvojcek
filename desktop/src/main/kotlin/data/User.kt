package data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String
)
