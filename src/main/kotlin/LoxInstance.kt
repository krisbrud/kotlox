class LoxInstance(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()

    override fun toString(): String = "${klass.name} instance"

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }
}