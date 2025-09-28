class LoxClass(
    val name: String,
    private val methods: MutableMap<String, LoxFunction>,
) : LoxCallable {
    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? {
        return if (methods.containsKey(name)) {
            methods[name]
        } else null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        return instance
    }

    override val arity: Int
        get() = 0
}