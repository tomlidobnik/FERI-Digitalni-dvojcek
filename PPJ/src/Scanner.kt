class Scanner(private val input: String) {
    private var index = 0
    private var line = 1
    private var column = 1
    private val tokens = mutableListOf<Token>()
    private var current = 0

    init {
        scanTokens()
    }

    private fun scanTokens() {
        while (index < input.length) {
            val ch = input[index]
            when (ch) {
                ' ', '\r', '\t' -> advance()
                '\n' -> advanceLine()

                '{' -> addToken(TokenType.LBRACE, "{")
                '}' -> addToken(TokenType.RBRACE, "}")
                '[' -> addToken(TokenType.LBRACKET, "[")
                ']' -> addToken(TokenType.RBRACKET, "]")
                '(' -> addToken(TokenType.LPAREN, "(")
                ')' -> addToken(TokenType.RPAREN, ")")
                ',' -> addToken(TokenType.COMMA, ",")
                ';' -> addToken(TokenType.SEMICOLON, ";")
                '=' -> addToken(TokenType.EQUAL, "=")
                '+' -> addToken(TokenType.PLUS, "+")
                '-' -> addToken(TokenType.MINUS, "-")
                '?' -> addToken(TokenType.QUESTION_MARK, "?")
                '@' -> addToken(TokenType.AT, "@")
                '$' -> addToken(TokenType.DOLLAR, "$")
                '"' -> {
                    val startColumn = column
                    advance() // skip opening "
                    val str = readWhile { it != '"' }
                    advance() // skip closing "
                    tokens.add(Token(TokenType.STRING, str, line, startColumn))
                }
                in '0'..'9' -> {
                    val startColumn = column
                    val number = readNumber()
                    tokens.add(Token(TokenType.NUMBER, number, line, startColumn))
                }
                else -> {
                    if (ch.isLetter() || ch == '_') {
                        val startColumn = column
                        val word = readWhile { it.isLetterOrDigit() || it == '_' }
                        val type = when (word) {
                            "city" -> TokenType.CITY
                            "road" -> TokenType.ROAD
                            "building" -> TokenType.BUILDING
                            "area" -> TokenType.AREA
                            "lake" -> TokenType.LAKE
                            "park" -> TokenType.PARK
                            "polyline" -> TokenType.POLYLINE
                            "polygon" -> TokenType.POLYGON
                            "circle" -> TokenType.CIRCLE
                            "let" -> TokenType.LET
                            "fst" -> TokenType.FST
                            "snd" -> TokenType.SND
                            else -> TokenType.ID
                        }
                        tokens.add(Token(type, word, line, startColumn))
                    } else {
                        throw IllegalArgumentException("Unexpected character: '$ch' at line $line, column $column")
                    }
                }
            }
        }
        tokens.add(Token(TokenType.EOF, "", line, column))
    }

    private fun advance() {
        index++
        column++
    }

    private fun advanceLine() {
        index++
        line++
        column = 1
    }

    private fun addToken(type: TokenType, lexeme: String) {
        tokens.add(Token(type, lexeme, line, column))
        advance()
    }

    fun nextToken(): Token {
        return if (current < tokens.size) tokens[current++] else tokens.last()
    }

    private fun readWhile(condition: (Char) -> Boolean): String {
        val start = index
        while (index < input.length && condition(input[index])) {
            advance()
        }
        return input.substring(start, index)
    }

    private fun readNumber(): String {
        val start = index
        while (index < input.length && input[index].isDigit()) {
            advance()
        }
        if (index < input.length && input[index] == '.') {
            advance()
            while (index < input.length && input[index].isDigit()) {
                advance()
            }
        }
        return input.substring(start, index)
    }
}
