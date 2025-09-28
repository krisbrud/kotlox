import java.io.File
import java.io.PrintWriter

val exprGrammar = """
Assign      : Token name, Expr value
Binary      : Expr left, Token operator, Expr right
Call        : Expr callee, Token paren, List<Expr> arguments
Get         : Expr obj, Token name
Grouping    : Expr expression
Literal     : Any? value
Logical     : Expr left, Token operator, Expr right
Set         : Expr obj, Token name, Expr value
Super       : Token keyword, Token method
This        : Token keyword
Unary       : Token operator, Expr right
Variable    : Token name
""".trimIndent()

val stmtGrammar = """
Block       : List<Stmt> statements
Class       : Token name, Expr.Variable? superclass, List<Stmt.Function> methods
Expression  : Expr expression
Function    : Token name, List<Token> params, List<Stmt> body
If          : Expr condition, Stmt thenBranch, Stmt? elseBranch
Print       : Expr expression
Return      : Token keyword, Expr? value
Var         : Token name, Expr initializer
While       : Expr condition, Stmt body
""".trimIndent()

val indent = "    "

fun defineAst(
    outputDir: String,
    baseName: String,
    types: List<String>
) {
    File("$outputDir/$baseName.kt").printWriter().use { writer ->
        writer.println("sealed interface $baseName {")
        writer.println(indent + "fun <R> accept(visitor: Visitor<R>): R")
        writer.println()

        types.forEach { type ->
            val className = type.split(":")[0].trim()
            val fields = type.split(":")[1].trim()
            defineType(writer, baseName, className, fields)
        }

        defineVisitor(writer, baseName, types)
        writer.println("}")
    }
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    writer.println(indent + "data class $className(")

    // Set fields in constructor
    val fields = fieldList.split(", ")
    fields.forEach { field ->
        val type = field.split(" ")[0].trim()
        val name = field.split(" ")[1].trim()
        writer.println(indent + indent + "val $name: $type,")
    }

    // End the constructor
    writer.println(indent + ") : $baseName {")

    // Add visitor
    //         override fun <R> accept(visitor: Visitor<R>) = visitor.visitGroupingExpr(this)
    writer.println(indent + indent + "override fun <R> accept(visitor: Visitor<R>) = visitor.visit${className}$baseName(this)")

    // End class body
    writer.println(indent + "}")
    writer.println()
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println(indent + "interface Visitor<R> {")

    types.forEach { type ->
        val typeName = type.split(":")[0].trim()
        writer.println(indent + indent +  "fun visit${typeName}$baseName(${baseName.lowercase()}: $typeName): R")

    }
    writer.println("    } ")
}

defineAst(
    outputDir = "src/main/kotlin/",
    baseName = "Expr",
    types = exprGrammar.lines()
)

defineAst(
    outputDir = "src/main/kotlin/",
    baseName = "Stmt",
    types = stmtGrammar.lines()
)
