package model

data class User(
    val id: Int,
    val username: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String
)
