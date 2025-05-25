package model

data class Location(
    val id: Int,
    val info: String,
    val longitude: Double,
    val latitude: Double,
    val location_outline_fk: Int
)

