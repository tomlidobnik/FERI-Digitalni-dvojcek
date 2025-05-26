package model

import java.time.LocalDateTime

data class ChatMessage(
    val id: Int,
    val user_fk: Int,
    val message: String,
    val created_at: LocalDateTime,
    val event_fk: Int
)
