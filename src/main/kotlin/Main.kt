import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset


val KEYWORDS: Map<String, TokenType> = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
)

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int
) {
    override fun toString(): String = "$type $lexeme $literal"
}

const val NULL_CHAR: Char = '\u0000'

data class Scanner(
    val source: String,
    val errorFunc: (Int, String) -> Unit,
) {
    var tokens: MutableList<Token> = mutableListOf()
    var start: Int = 0
    var current: Int = 0
    var line: Int = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    fun advance(): Char {
        return source[current++]
    }

    fun addToken(type: TokenType) {
        addToken(type, null)
    }

    fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    fun peek(): Char {
        if (isAtEnd()) return NULL_CHAR
        return source.get(current)
    }

    fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            errorFunc(line, "unterminated string")
            return
        }

        // Closing "
        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    fun number() {
        while (isDigit(peek())) {
            advance()
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        val value = source.substring(start, current).toDoubleOrNull() ?: errorFunc(line, "Couldn't parse double")
        addToken(TokenType.NUMBER, value)
    }

    fun isAlpha(c: Char): Boolean = (c in 'a'..'z') || (c in 'A'..'Z') || c == '_'

    fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    fun identifier() {
        while (isAlphaNumeric(peek())) advance();

        val text = source.substring(start, current)
        val type = KEYWORDS.get(text) ?: TokenType.IDENTIFIER

        addToken(type);
    }

    fun peekNext(): Char {
        if (current + 1 >= source.length) {
            return NULL_CHAR
        } else {
            return source[current + 1]
        }
    }

    fun scanToken() {
        val c = advance()

        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)

            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    // comment until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            ' ', '\r', '\t' -> {
                // Do nothing
            }

            '\n' -> {
                line++
            }

            '"' -> {
                string()
            }


            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    errorFunc(line, "unexpected character '$c'")
                }
            }
        }
    }

    fun isAtEnd(): Boolean = current >= source.length

    fun isDigit(c: Char): Boolean = c in '0'..'9'
}

class Lox {
    var hadError = false

    fun run(source: String) {
        val scanner = Scanner(
            source,
            { line: Int, msg: String -> error(line, msg) }
        )
        val tokens: List<Token> = scanner.scanTokens()

        // Just print the tokens for now
        for (token in tokens) {
            println(token)
        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
        hadError = true
    }

    fun report(line: Int, where: String, message: String) {
        println("[line $line] Error $where : $message")
    }

    fun runFile(path: String) {
        val source = File(path).readText(charset = Charset.defaultCharset())
        run(source)

        if (hadError) System.exit(65)
    }

    fun runPrompt() {
        println("In runPrompt()")
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: kotlox [script]")
            System.exit(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }
}

fun main(args: Array<String>) {
    val lox = Lox()

    lox.main(args)
}
