# bicycle

A simple programming language similar to C which is parsed and executed using Java.

This project is for educational purposes. It provides basics of how to write a programming language.

## How to run

Just run the [Main.java](./src/Main.java) file.

## Supported features

### Input & Output

- Input is **not** supported.
- Output is supported using `printf` function.

### Assignment

Assignments are implemented by single `=`;

### Arithmetic operations

- `+` addition
- `-` subtraction
- `*` multiplication
- `/` division
- `%` modulus

### Conditional operators

- `if`
- `elseif` (together)
- `else`

##### Comparison operators

- `!` negation.
- `!=` not equal.
- `==` equal.
- `<` less than.
- `>` greater than.
- `<=` less or equal.
- `>=` greater or equal.
- `&&` and.
- `||` or.

### Cycle operators

- `for` - **supported**. For-loops can be stopped by `break`.
- `while` is **not** supported.
- `do-while` is **not** supported.

### Types

- Boolean `boolean`
- Integer `int`
- Double `double`
- String `string`
- Void `void` for functions.
- Struct `struct` for creating custom structures. A new instance of a struct can be created using `new` keyword.
- Null `null`. Any variable can be null.

### Arrays

Arrays are **not** supported.

### Functions

- Functions arguments can have default value `void func(int p = 0)`.
- Functions arguments with the same type can be chained `int totalChars(string a, b, c)`.
- Recursion is **supported**.

_:exclamation: There are could be functionalities that are not listed above. For the full list of functionalities have a
look at the language's [grammar](./src/grammar.txt) file._

_The language is close as much as possible
to the C language, so just use your imagination :wink:_