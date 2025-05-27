enum class TokenType {
    // Keywords
    LET, QUESTION_MARK, CITY,
    ROAD, BUILDING, AREA, LAKE, PARK,
    POLYLINE, POLYGON, CIRCLE,

    // first, second
    FST, SND,

    // Symbols
    LBRACE, RBRACE,       // { }
    LBRACKET, RBRACKET,   // [ ]
    LPAREN, RPAREN,       // ( )
    COMMA, SEMICOLON,     // , ;
    EQUAL, PLUS, MINUS,   // = + -

    // Literals
    NUMBER, STRING, ID,

    // Special
    DOLLAR, AT, EOF
}
