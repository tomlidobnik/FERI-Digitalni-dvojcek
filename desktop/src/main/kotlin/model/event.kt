package model

import java.time.LocalDateTime

data class Event(
    val id: Int,
    val user_fk: Int,
    val title: String,
    val description: String,
    val start_date: LocalDateTime,
    val end_date: LocalDateTime,
    val location_fk: Int,
    val public: Boolean
)
