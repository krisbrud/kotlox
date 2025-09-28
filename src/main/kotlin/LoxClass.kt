class LoxClass(val name: String) : LoxCallable {
    override fun toString(): String = name

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        return instance
    }

    override val arity: Int
        get() = 0
}