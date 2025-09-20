class Environment(
    val enclosing: Environment? = null,
) {
    private val values: MutableMap<String, Any?> = mutableMapOf()
    fun get(name: Token): Any? {
        return values.getOrElse(name.lexeme) {
            if (enclosing != null) return enclosing.get(name)

            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else if (enclosing != null) {
            enclosing.assign(name, value)
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}