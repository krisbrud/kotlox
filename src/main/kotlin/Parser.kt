data class Parser(
    val tokens: List<Token>,
    val errorReporter: (Token, String) -> Unit,
) {
    class ParseError() : RuntimeException()

    var current: Int = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            val decl = declaration()
            if (decl != null) {
                statements.add(decl)
            }
        }

        return statements
    }

    private fun declaration(): Stmt? {
        try {
            return if (match(TokenType.CLASS)) classDeclaration()
            else if (match(TokenType.FUN)) function("function")
            else if (match(TokenType.VAR)) varDeclaration()
            else statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun classDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Function>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"))
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.")

        return Stmt.Class(name, methods)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

        val initializer: Expr = if (match(TokenType.EQUAL)) {
            expression()
        } else {
            Expr.Literal(null)
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        return if (match(TokenType.FOR)) forStatement()
        else if (match(TokenType.IF)) ifStatement()
        else if (match(TokenType.PRINT)) printStatement()
        else if (match(TokenType.RETURN)) returnStatement()
        else if (match(TokenType.WHILE)) whileStatement()
        else if (match(TokenType.LEFT_BRACE)) Stmt.Block(block())
        else expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? = if (match(TokenType.SEMICOLON)) {
            null
        } else if (match(TokenType.VAR)) {
            varDeclaration()
        } else {
            expressionStatement()
        }

        var condition: Expr? = if (!check(TokenType.SEMICOLON)) {
            expression()
        } else null
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        val increment: Expr? = if (!check(TokenType.RIGHT_PAREN)) {
            expression()
        } else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(
                listOf(body, Stmt.Expression(increment))
            )
        }

        if (condition == null) {
            condition = Expr.Literal(true)
        }
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect '(' after 'if'.")

        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) {
            statement()
        } else null

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.also {
                statements.add(it)
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value = if (!check(TokenType.SEMICOLON)) {
            expression()
        } else null

        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(value)
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect $kind name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters: MutableList<Token> = mutableListOf()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }

                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")

        consume(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    fun expression(): Expr = assignment()

    fun assignment(): Expr {
        val expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }


    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    fun comparison(): Expr {
        var expr = term()

        while (match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL,
            )
        ) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    fun term(): Expr {
        var expr = factor()

        while (match(
                TokenType.MINUS,
                TokenType.PLUS,
            )
        ) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    fun factor(): Expr {
        var expr = unary()

        while (match(
                TokenType.SLASH,
                TokenType.STAR,
            )
        ) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, paren, arguments)
    }

    fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression")
    }


    // Helper methods

    fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN -> return // Continue parsing expressions
                else -> Unit // keep discarding tokens
            }

            advance()
        }
    }

    fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    fun error(token: Token, message: String): ParseError {
        errorReporter(token, message)
        return ParseError()
    }

    fun match(vararg types: TokenType): Boolean {
        types.forEach { type ->
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    fun peek(): Token = tokens[current]

    fun previous(): Token = tokens[current - 1]
}