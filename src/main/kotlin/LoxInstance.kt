class LoxInstance(private val klass: LoxClass) {
    override fun toString(): String = "${klass.name} instance"
}