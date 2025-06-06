sealed interface Expr

data class VariableDefinition(val name: String, val value: Point) : Expr
data class City(val name: String, val blocks: List<Block>) : Expr
data class Query(val points: List<Point>, val center: Point, val radius: Expr) : Expr

// --- Izrazi ---
data class NumberLiteral(val value: Double) : Expr
data class VariableReference(val name: String) : Expr
data class FirstCoordinate(val point: Expr) : Expr
data class SecondCoordinate(val point: Expr) : Expr

// --- Bloki v mestu ---
sealed interface Block
data class Road(val name: String, val polyline: Polyline) : Block
data class Building(val name: String, val shape: Shape) : Block
data class Area(val name: String, val shape: Shape) : Block
data class Lake(val name: String, val shape: Shape) : Block
data class Park(val name: String, val shape: Shape) : Block

// --- Oblike ---
sealed interface Shape
data class Polygon(val points: List<Point>) : Shape
data class Circle(val center: Point, val radius: Expr) : Shape

// --- Polylines ---
data class Polyline(val points: List<Point>)

// --- Točke ---
data class Point(val components: List<PointComponent>)

sealed interface PointComponent
data class CoordinatePair(val x: Expr, val y: Expr) : PointComponent
data class VariableRef(val name: String) : PointComponent
data class First(val point: Point) : PointComponent
data class Second(val point: Point) : PointComponent

fun Expr.toGeoJSON(variables: List<VariableDefinition>): String {
    return when (this) {
        is City -> {
            val features = blocks.map { it.toGeoJSONFeature(variables) }
            """{
                "type": "FeatureCollection",
                "features": [${features.joinToString(",")}]
            }"""
        }
        else -> throw IllegalArgumentException("Unsupported Expr type for GeoJSON conversion")
    }
}

fun Block.toGeoJSONFeature(variables: List<VariableDefinition>): String {
    return when (this) {
        is Road -> """{
            "type": "Feature",
            "geometry": {
                "type": "LineString",
                "coordinates": ${polyline.points.toGeoJSONCoordinates(variables)}
            },
            "properties": {"name": "$name"}
        }"""
        is Building -> """{
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [${(shape as Polygon).points.toGeoJSONCoordinates(variables)}]
            },
            "properties": {"name": "$name"}
        }"""
        is Area -> """{
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [${(shape as Polygon).points.toGeoJSONCoordinates(variables)}]
            },
            "properties": {"name": "$name"}
        }"""
        is Park -> """{
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [${(shape as Polygon).points.toGeoJSONCoordinates(variables)}]
            },
            "properties": {"name": "$name"}
        }"""
        is Lake -> """{
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [${(shape as Polygon).points.toGeoJSONCoordinates(variables)}]
            },
            "properties": {"name": "$name"}
        }"""
        else -> throw IllegalArgumentException("Unsupported Block type for GeoJSON conversion")
    }
}

fun List<Point>.toGeoJSONCoordinates(variables: List<VariableDefinition>): String {
    val nestedCoordinates = map { it.toGeoJSONCoordinates(variables) }
    return "[${nestedCoordinates.joinToString(",")}]"
}

fun Point.toGeoJSONCoordinates(variables: List<VariableDefinition>): String {
    val pair = components.filterIsInstance<CoordinatePair>().firstOrNull()
    if (pair != null) {
        return "[${pair.x.toGeoJSONValue()}, ${pair.y.toGeoJSONValue()}]"
    }

    val variableRef = components.filterIsInstance<VariableRef>().firstOrNull()
    if (variableRef != null) {
        val resolvedPoint = resolveVariableRef(variableRef, variables)
        return resolvedPoint.toGeoJSONCoordinates(variables)
    }

    throw IllegalArgumentException("Point must contain a CoordinatePair or a resolvable VariableRef.")
}

fun Expr.toGeoJSONValue(): String {
    return when (this) {
        is NumberLiteral -> value.toString()
        else -> throw IllegalArgumentException("Unsupported Expr type for GeoJSON value conversion")
    }
}

fun resolveVariableRef(ref: VariableRef, variables: List<VariableDefinition>): Point {
    val definition = variables.find { it.name == ref.name }
        ?: throw IllegalArgumentException("VariableRef '${ref.name}' is not defined.")
    return definition.value
}