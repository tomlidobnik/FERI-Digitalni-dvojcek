package model

import java.time.LocalDateTime

data class FriendChatMessage(
    val id: Int,
    val user_fk: Int,
    val message: String,
    val created_at: LocalDateTime,
    val friend_fk: Int
)
