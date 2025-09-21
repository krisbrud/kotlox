class LoxFunction(private val declaration: Stmt.Function) : LoxCallable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
        declaration.params.forEachIndexed { i, token ->
            environment.define(token.lexeme, arguments[i])
        }

        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}