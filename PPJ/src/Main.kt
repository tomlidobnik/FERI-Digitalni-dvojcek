import java.io.File
//
//fun main() {
//    val input = File("src/examples.txt").readText()
//    val scanner = Scanner(input)
//
//    println("Tokens:")
//    while (true) {
//        val token = scanner.nextToken()
//        println(token)
//        if (token.type == TokenType.EOF) break
//    }
//
//    val parser = Parser(Scanner(input))
//
//    try {
//        parser.parseExpr()
//        println("Parsing successful.")
//    } catch (e: Exception) {
//        println("Parsing failed: ${e.message}")
//    }
//}


fun main() {
    val input = File("src/examples.txt").readText()
    val scanner = Scanner(input)
    val parser = Parser(scanner)

    try {
        val ast = parser.parseExpr()
        println("AST:")
        ast.forEach { println(it) }
        println("Parsing is a success")

        val variables = listOf(
            VariableDefinition("p", Point(listOf(CoordinatePair(NumberLiteral(1.0), NumberLiteral(1.12))))),
            VariableDefinition("q", Point(listOf(CoordinatePair(NumberLiteral(2.0), NumberLiteral(2.0))))),
            VariableDefinition("r", Point(listOf(CoordinatePair(NumberLiteral(1.0), NumberLiteral(2.0)))))
        )

        val geoJSON = ast.filterIsInstance<City>().joinToString(",") { it.toGeoJSON(variables) }
        println("GeoJSON:")
        println(geoJSON)
        File("geojsonOut.json").writeText(geoJSON)

    } catch (e: Exception) {
        println("Parsing failed: ${e.message}")
    }
}