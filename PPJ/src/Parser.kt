import TokenType.*

class Parser(private val scanner: Scanner) {

    private var currentToken: Token = scanner.nextToken()

    private fun readToken(expectedType: TokenType): Token {
        if (currentToken.type == expectedType) {
            val token = currentToken
            currentToken = scanner.nextToken()
            return token
        } else {
            throw IllegalArgumentException("Expected token $expectedType but got ${currentToken.type} at line ${currentToken.line}, at column ${currentToken.column} with lexeme '${currentToken.lexeme}'")
        }
    }

    fun parseExpr(): List<Expr> {
        val exprs = mutableListOf<Expr>()
        while (currentToken.type in listOf(LET, QUESTION_MARK, CITY)) {
            val expr = when (currentToken.type) {
                LET -> parseVariableDef()
                QUESTION_MARK -> parseQuery()
                CITY -> parseCity()
                else -> throw IllegalArgumentException("Unexpected token ${currentToken.type}")
            }
            exprs.add(expr)
        }
        return exprs
    }

    private fun parseCity(): City {
        readToken(CITY)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val blocks = parseBlocks()
        readToken(RBRACE)
        return City(name, blocks)
    }

    private fun parseBlocks(): List<Block> {
        val blocks = mutableListOf<Block>()
        while (currentToken.type in listOf(ROAD, BUILDING, AREA, LAKE, PARK)) {
            val block: Block = when (currentToken.type) {
                ROAD -> parseRoad()
                BUILDING -> parseBuilding()
                AREA -> parseArea()
                LAKE -> parseLake()
                PARK -> parsePark()
                else -> throw IllegalArgumentException("Unexpected block type: ${currentToken.type}")
            }
            blocks.add(block)
        }
        return blocks
    }

    private fun parseRoad(): Road {
        readToken(ROAD)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val polyline = parsePolyline()
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Road(name, polyline)
    }

    private fun parseBuilding(): Building {
        readToken(BUILDING)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val shape = parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Building(name, shape)
    }

    private fun parseArea(): Area {
        readToken(AREA)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val shape = parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Area(name, shape)
    }

    private fun parseLake(): Lake {
        readToken(LAKE)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val shape = parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Lake(name, shape)
    }

    private fun parsePark(): Park {
        readToken(PARK)
        readToken(LBRACKET)
        val name = readToken(STRING).lexeme
        readToken(RBRACKET)
        readToken(LBRACE)
        val shape = parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Park(name, shape)
    }

    private fun parseShape(): Shape {
        return when (currentToken.type) {
            POLYGON -> parsePolygon()
            CIRCLE -> parseCircle()
            else -> throw IllegalArgumentException("Expected POLYGON or CIRCLE but got ${currentToken.type}")
        }
    }

    private fun parsePolyline(): Polyline {
        readToken(POLYLINE)
        readToken(LBRACKET)
        val points = parsePoints()
        readToken(RBRACKET)
        readToken(SEMICOLON)
        return Polyline(points)
    }

    private fun parsePolygon(): Polygon {
        readToken(POLYGON)
        readToken(LBRACKET)
        val points = parsePoints()
        readToken(RBRACKET)
        readToken(SEMICOLON)
        return Polygon(points)
    }

    private fun parseCircle(): Circle {
        readToken(CIRCLE)
        readToken(LBRACKET)
        val center = parsePoint()
        readToken(COMMA)
        val radius = parseCircleValue()
        readToken(RBRACKET)
        readToken(SEMICOLON)
        return Circle(center, radius)
    }

    private fun parseCircleValue(): Expr {
        return when (currentToken.type) {
            FST, SND -> parseFirstSecond()
            NUMBER -> {
                val numberToken = readToken(NUMBER)
                NumberLiteral(numberToken.lexeme.toDouble())
            }
            else -> throw IllegalArgumentException("Expected FST/SND or NUMBER in circle but got ${currentToken.type}")
        }
    }

    private fun parsePoints(): List<Point> {
        val points = mutableListOf<Point>()
        points.add(parsePoint())
        while (currentToken.type == COMMA) {
            readToken(COMMA)
            points.add(parsePoint())
        }
        return points
    }

    private fun parsePoint(): Point {
        val components = mutableListOf<PointComponent>()
        components.add(parsePointComponent())

        while (currentToken.type == PLUS || currentToken.type == MINUS) {
            val op = readToken(currentToken.type).type
            val next = parsePointComponent()
            components.add(next)
        }

        return Point(components)
    }

    private fun parsePointComponent(): PointComponent {
        return when (currentToken.type) {
            LPAREN -> {
                readToken(LPAREN)
                val x = parseCoordinate()
                readToken(COMMA)
                val y = parseCoordinate()
                readToken(RPAREN)
                CoordinatePair(x, y)
            }
            DOLLAR -> {
                readToken(DOLLAR)
                val name = readToken(ID).lexeme
                VariableRef(name)
            }
            FST -> {
                readToken(FST)
                val inner = parsePoint()
                First(inner)
            }
            SND -> {
                readToken(SND)
                val inner = parsePoint()
                Second(inner)
            }
            else -> throw IllegalArgumentException("Expected point or variable in POINT but got ${currentToken.type}")
        }
    }

    private fun parseCoordinate(): Expr {
        return when (currentToken.type) {
            NUMBER -> {
                val value = readToken(NUMBER).lexeme.toDouble()
                NumberLiteral(value)
            }
            ID -> {
                val name = readToken(ID).lexeme
                VariableReference(name)
            }
            FST -> {
                readToken(FST)
                val inner = parseCoordinate()
                FirstCoordinate(inner)
            }
            SND -> {
                readToken(SND)
                val inner = parseCoordinate()
                SecondCoordinate(inner)
            }
            LPAREN -> {
                readToken(LPAREN)
                val expr = parseCoordinate()
                readToken(RPAREN)
                expr
            }
            else -> throw IllegalArgumentException("Expected coordinate expression but got ${currentToken.type}, line: ${currentToken.line}, col: ${currentToken.column} ")
        }
    }

    private fun parseFirstSecond(): Expr {
        return when (currentToken.type) {
            FST -> {
                readToken(FST)
                val inner = parseCoordinate()
                FirstCoordinate(inner)
            }
            SND -> {
                readToken(SND)
                val inner = parseCoordinate()
                SecondCoordinate(inner)
            }
            else -> throw IllegalArgumentException("Expected FST or SND but got ${currentToken.type}")
        }
    }

    private fun parseVariableDef(): VariableDefinition {
        readToken(LET)
        readToken(AT)
        val name = readToken(ID).lexeme
        readToken(EQUAL)
        val value = parsePoint()
        readToken(SEMICOLON)
        return VariableDefinition(name, value)
    }

    private fun parseQuery(): Query {
        readToken(QUESTION_MARK)
        readToken(LBRACE)
        readToken(LBRACKET)
        val points = parsePoints()
        readToken(RBRACKET)
        readToken(COMMA)
        readToken(LBRACKET)
        val center = parsePoint()
        readToken(COMMA)
        val radius = parseCircleValue()
        readToken(RBRACKET)
        readToken(RBRACE)
        readToken(SEMICOLON)
        return Query(points, center, radius)
    }
}
