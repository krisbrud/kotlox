sealed interface Stmt {
    fun <R> accept(visitor: Visitor<R>): R

    data class Expression(
        val expression: Expr,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visitExpressionExpr(this)
    }

    data class Print(
        val expression: Expr,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visitPrintExpr(this)
    }

    interface Visitor<R> {
        fun visitExpressionExpr(stmt: Expression): R
        fun visitPrintExpr(stmt: Print): R
    } 
}
