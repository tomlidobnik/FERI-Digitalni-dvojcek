import TokenType.*

class Parser(private val scanner: Scanner) {

    private var currentToken: Token = scanner.nextToken()

    private fun readToken(expectedType: TokenType): Token {
        if (currentToken.type == expectedType) {
            val token = currentToken
            currentToken = scanner.nextToken()
            return token
        } else {
            throw IllegalArgumentException("Expected token $expectedType but got ${currentToken.type}")
        }
    }

    fun parseExpr() {
        while (currentToken.type in listOf(LET, QUESTION_MARK, CITY)) {
            when (currentToken.type) {
                LET -> parseVariableDef()
                QUESTION_MARK -> parseQuery()
                CITY -> parseCity()
                else -> throw IllegalArgumentException("Unexpected token ${currentToken.type}")
            }
        }
    }

    private fun parseCity() {
        readToken(CITY)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parseBlocks()
        readToken(RBRACE)
    }

    private fun parseBlocks() {
        while (currentToken.type in listOf(ROAD, BUILDING, AREA, LAKE, PARK)) {
            when (currentToken.type) {
                ROAD -> parseRoad()
                BUILDING -> parseBuilding()
                AREA -> parseArea()
                LAKE -> parseLake()
                PARK -> parsePark()
                else -> throw IllegalArgumentException("Unexpected block type: ${currentToken.type}")
            }
        }
    }

    private fun parseRoad() {
        readToken(ROAD)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parsePolyline()
        readToken(RBRACE)
        readToken(SEMICOLON)
    }

    private fun parseBuilding() {
        readToken(BUILDING)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
    }

    private fun parseArea() {
        readToken(AREA)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
    }

    private fun parseLake() {
        readToken(LAKE)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
    }

    private fun parsePark() {
        readToken(PARK)
        readToken(LBRACKET)
        readToken(STRING)
        readToken(RBRACKET)
        readToken(LBRACE)
        parseShape()
        readToken(RBRACE)
        readToken(SEMICOLON)
    }

    private fun parseShape() {
        when (currentToken.type) {
            POLYGON -> parsePolygon()
            CIRCLE -> parseCircle()
            else -> throw IllegalArgumentException("Expected POLYGON or CIRCLE but got ${currentToken.type}")
        }
    }

    private fun parsePolyline() {
        readToken(POLYLINE)
        readToken(LBRACKET)
        parsePoints()
        readToken(RBRACKET)
        readToken(SEMICOLON)
    }

    private fun parsePolygon() {
        readToken(POLYGON)
        readToken(LBRACKET)
        parsePoints()
        readToken(RBRACKET)
        readToken(SEMICOLON)
    }

    private fun parseCircle() {
        readToken(CIRCLE)
        readToken(LBRACKET)
        parsePoint()
        readToken(COMMA)
        parseCircleValue()
        readToken(RBRACKET)
        readToken(SEMICOLON)
    }

    private fun parseCircleValue() {
        when (currentToken.type) {
            FST, SND -> parseFirstSecond()
            NUMBER -> readToken(NUMBER)
            else -> throw IllegalArgumentException("Expected FST/SND or NUMBER in circle but got ${currentToken.type}")
        }
    }

    private fun parsePoints() {
        parsePoint()
        while (currentToken.type == COMMA) {
            readToken(COMMA)
            parsePoint()
        }
    }

    private fun parsePoint() {
        parsePointComponent()
        while (currentToken.type == PLUS || currentToken.type == MINUS) {
            readToken(currentToken.type)
            parsePointComponent()
        }
    }

    private fun parsePointComponent() {
        when (currentToken.type) {
            LPAREN -> {
                readToken(LPAREN)

                val tempToken = currentToken
                parseCoordinate()

                if (currentToken.type == COMMA) {

                    readToken(COMMA)
                    parseCoordinate()
                } else {
                    // do nothing
                }

                readToken(RPAREN)
            }

            DOLLAR -> {
                readToken(DOLLAR)
                readToken(ID)
            }

            FST, SND -> parseFirstSecond()

            else -> throw IllegalArgumentException("Expected point or variable in POINT but got ${currentToken.type}")
        }
    }


    private fun parseCoordinate() {
        when (currentToken.type) {
            NUMBER -> readToken(NUMBER)
            FST, SND -> parseFirstSecond()
            ID -> readToken(ID)
            else -> throw IllegalArgumentException("Expected NUMBER, ID or FIRST_SECOND in COORDINATE but got ${currentToken.type}")
        }
    }


    private fun parseFirstSecond() {
        when (currentToken.type) {
            FST -> {
                readToken(FST)
                parsePoint()
            }
            SND -> {
                readToken(SND)
                parsePoint()
            }
            else -> throw IllegalArgumentException("Expected FST or SND but got ${currentToken.type}")
        }
    }

    private fun parseVariableDef() {
        readToken(LET)
        readToken(AT)
        readToken(ID)
        readToken(EQUAL)
        parsePoint()
        readToken(SEMICOLON)
    }

    private fun parseQuery() {
        readToken(QUESTION_MARK)
        readToken(LBRACE)
        readToken(LBRACKET)
        parsePoints()
        readToken(RBRACKET)
        readToken(COMMA)
        readToken(LBRACKET)
        parsePoint()
        readToken(COMMA)
        parseCircleValue()
        readToken(RBRACKET)
        readToken(RBRACE)
        readToken(SEMICOLON)
    }
}
