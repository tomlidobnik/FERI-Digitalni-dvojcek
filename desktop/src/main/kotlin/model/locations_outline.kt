package model

data class Point(val type: String = "Point", val coordinates: List<Double>)

data class LocationOutline(
    val id: Int,
    val point: String // JSONB as a JSON-encoded string
)
