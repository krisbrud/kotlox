class Environment(
    val enclosing: Environment? = null,
) {
    private val values: MutableMap<String, Any?> = mutableMapOf()
//    val values: MutableMap<String, Any?> = mutableMapOf()
    fun get(name: Token): Any? {
        return values.getOrElse(name.lexeme) {
            if (enclosing != null) return enclosing.get(name)

            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values.get(name)
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        for (i in 0..<distance) {
            environment = environment.enclosing ?: throw RuntimeException("Finding ancestor failed!")
        }

        return environment
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