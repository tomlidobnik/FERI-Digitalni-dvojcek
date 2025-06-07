package data

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    val longitude: Double,
    val latitude: Double
)

@Serializable
data class LocationOutline(
    val id: Int? = null,
    val info: String? = null,
    val points: List<Coordinate>
)
