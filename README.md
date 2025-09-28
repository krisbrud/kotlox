# kotlox

A tree-walk interpreter for the toy language [Lox](https://craftinginterpreters.com/the-lox-language.html) from the book [Crafting Interpreters](https://craftinginterpreters.com) by Robert Nystrom, written in Kotlin.

## Getting started

Requires `java` to be installed on your machine, the project used `openjdk 21.0.2`.

Build the project and make a fat jar:
```shell
./gradlew build fatJar
```

Run the REPL:
```shell
./kotlox
```

Run a program:
```shell
./kotlox programs/whilefib.lox
```