package data

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int? = null,
    val info: String,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val location_outline_fk: Int? = null
)