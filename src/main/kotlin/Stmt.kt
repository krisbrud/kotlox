sealed interface Stmt {
    fun <R> accept(visitor: Visitor<R>): R

    data class Block(
        val statements: List<Stmt>,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visitBlockExpr(this)
    }

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

    data class Var(
        val name: Token,
        val initializer: Expr,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visitVarExpr(this)
    }

    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R
        fun visitExpressionStmt(stmt: Expression): R
        fun visitPrintStmt(stmt: Print): R
        fun visitVarStmt(stmt: Var): R
    } 
}
