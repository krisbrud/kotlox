class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : LoxCallable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.params.forEachIndexed { i, token ->
            environment.define(token.lexeme, arguments[i])
        }

        try{
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }

        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}