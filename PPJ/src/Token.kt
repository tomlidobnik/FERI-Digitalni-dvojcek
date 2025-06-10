data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int = 0,
    val column: Int = 0
)