package model

data class Friends(
    val id: Int,
    val user1_fk: Int,
    val user2_fk: Int,
    val status: String
)
