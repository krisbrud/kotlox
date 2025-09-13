import Expr.Binary
import Expr.Literal
import Expr.Unary

class AstPrinter : Expr.Visitor<String> {
    fun printExpr(expr: Expr): String = expr.accept(this)

    override fun visitBinaryExpr(expr: Binary): String = parenthesize(expr.operator.lexeme, expr.left, expr.right)
    override fun visitGroupingExpr(expr: Expr.Grouping): String = parenthesize("group", expr.expression)
    override fun visitLiteralExpr(expr: Literal): String = if (expr.value == null) "nil" else expr.value.toString()
    override fun visitUnaryExpr(expr: Unary): String = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr): String = "($name ${exprs.joinToString(" ") { it.accept(this) }})"
}

