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

