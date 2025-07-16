import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class Lox {
    fun run(source: String) {
        TODO("run not implemented")
    }

    fun runFile(path: String) {
        val source = File(path).readText(charset = Charset.defaultCharset())
        run(source)
    }

    fun runPrompt() {
        TODO("runPrompt not implemented!")
    }

    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: kotlox [script]")
            System.exit(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }
}

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}
