class RuntimeError(val token: Token, message: String) : RuntimeException(message)


class Interpreter(
    val errorReporter: (RuntimeError) -> Unit
) : Expr.Visitor<Any?> {
    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            errorReporter(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) <= (right as Double)
            }
            TokenType.BANG_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right)
            }
            TokenType.EQUAL_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right)
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                if ((left is Double) && (right is Double)) {
                    return left + right
                }

                if ((left is String) && (right is String)) {
                    return left + right
                }

                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }

            else -> {}
        }

        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }

            else -> {}
        }

        throw RuntimeError(expr.operator, "Unexpected unary opeartor.")
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return // :)
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if ((left is Double) && (right is Double)) return // All good
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(obj: Any?): Boolean {
        // Nil and false are falsey
        if (obj == null) return false
        if (obj is Boolean) return obj

        return true // Everything else is truthy
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            val text = obj.toString()

            return if (text.endsWith(".0")) {
                text.dropLast(2)
            } else text
        }

        return obj.toString()
    }
}