class Environment(
    private val values: MutableMap<String, Any?> = mutableMapOf()
) {
    fun get(name: Token): Any? {
        return values.getOrElse(name.lexeme) {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }
}