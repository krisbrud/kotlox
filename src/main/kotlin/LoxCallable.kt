
interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>)
}