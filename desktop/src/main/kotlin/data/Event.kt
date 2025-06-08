package data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Event(
    val id: Int? = null,
    val title: String,
    val description: String,
    val start_date: String,
    val end_date: String,
    val location_fk: Int,
    val public: Boolean,
    val tag: String
)
