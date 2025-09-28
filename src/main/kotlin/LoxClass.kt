class LoxClass(
    val name: String,
    val superclass: LoxClass?,
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
        val initializer = findMethod("init")

        // Call initializer if defined, otherwise just let the instance be a open collection of fields (properties?)
        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    override val arity: Int
        get() {
            val initializer = findMethod("init")
            return initializer?.arity ?: 0
        }
}