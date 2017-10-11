/**
 * Created by rane on 20/9/17.
 */
package learn.kotlin.funAdvanced

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


fun main(args: Array<String>) {
    println("Advanced - Higher order functions")

    val ints = listOf(1, 2, 3, 4, 5, 6)
    val odds = ints.filter({ it % 2 == 1 })     // Lambda: higher order function
    val evens = ints.filter { it % 2 == 0 }     // filter takes lambda, parenthesis optional

    // Anonymous functions: leave the name
    val evens = ints.filter(fun(k: Int) = k % 2 == 0) // using anonymous functions

    // What is the function signature of a function which takes another function
    fun foo(str: String, fn: (String) -> String): Unit {    // (String) -> String
        val applied = fn(str)
        println(applied)
    }

    //Returning a function
    fun modulo(k: Int): (Int) -> Boolean = { it % k == 0 }  // returning a lambda

    // Usage
    val ints = listOf(1, 2, 3, 4, 5, 6)
    val odd = ints.filter(modulo(1))
    val evens = ints.filter(modulo(2))

    val isEven: (Int) -> Boolean = modulo(2)    // hold function in a variable, that is lambda
    listOf(1, 2, 3, 4).filter(isEven)              // filter expects a variable or a lambda

    // OR
    val evens2 = ints.filter{ it -> it % 2 == 0}    // explicit input
    // OR
    val evens3 = ints.filter{it % 2 == 0}           // Input optional

    /*
    Multiple ways to define the variable (NOTE: there is not fun prefix, its val):

        val valEven: (Int) -> Boolean = modulo(2)  // hold function in a variable
        val valEven : (Int) -> Boolean = { it % 2 == 0 }
        val valEven = { k : Int -> k % 2 == 0 }

        if valEven is defined as a function, instead of a variable

        fun funEven(k: Int): Boolean = k % 2 == 0

        Then you cannot use is in the format
            listOf(1, 2, 3, 4).filter(valEven)  <-- this is error

        You need to use:

            listOf(1, 2, 3, 4).filter{ funEven(it)}

            OR

            listOf(1, 2, 3, 4).filter(::funEven)


        To convert a function, into a reference use ::
    */

    /* Member or extension function reference

    A function which takes 3 params, the last one is a function

        fun foo(a: Double, b: Double, f: (Double, Double) -> Double) = f(a, b)

    Regular way:
        foo(1.0, 2.0, { a, b -> Math.pow(a, b) })

    Kotlin way:
        foo(1.0, 2.0, Math::pow })

    Extension function:
        fun Int.isOdd(): Boolean = this % 1 == 0

    First way
        ints.filter { it.isOdd() }          // Actually calling the method

    Another way:
        ints.filter( Int::isOdd )           // Passing a reference


    What if the function is bound to a instance, like compare

        fun String.isSameAs(other: String) = this.toLowerCase() == other.toLowerCase()  // Note: this keyword

    The bound function's arity doesn't match because its bound.

    First way, create an unbound reference:

        listOf("foo", "moo", "boo").filter {
            (String::equalsIgnoreCase)("bar", it)
        }

    The kotlin way:
        listOf("foo", "baz", "BAR").filter("bar"::isSameAs) // "bar" get bound to this

    */

    /*

    Receiver of a function is the instance corresponds to the "this" keyword

    In Kotlin, function parameters can be defined to accept a receiver when
    they are invoked. We do that using the following syntax:

        fun foo(fn: String.() -> Boolean): Unit {    // function which takes a function as an argument
            "stringReceiver".fn()                    // which must be a extension method of a string
        }

    */

    /* Awesome kotlin has some real coolness. Function literals (statements between {} are very helpful
    for higher order functions. If a function takes its last input parameter as a function, you can define
    it as a function literal.
    Check this simple example out:
    */


    fun <T, U> withPrint(res: T, fn: (T) -> U): U  {  // function's second input is a function fn
        return fn(res)
    }

    fun cprint(str: String): Int {                      // a sample function
        println("Printing string inside cprint: ${str}")
        return str.length
    }

    withRes("Kotlin is cool", ::cprint)                 // this is fine, very predictable

    var str1 = "Kotlin is awesome"
    var len1 = withRes(str1) {                          // using function literals, the last input function
        print("Inside nowhere: ${str1}")                // is auto-magically used with a cool syntax!
        str1.length
    }
    print("Length of str ${len1}")

    // How do you use this coolMAXXX feature

    // function that handles resources in a safe manner: that is, the resource will always be
    // closed correctly, even if the code throws an exception
    fun <T: AutoCloseable, U> withResource(resource: T, fn: (T) -> U): U {
        try {
            return fn(resource)
        } finally {
            resource.close()
        }
    }

    // Usage:
    fun characterCount(filename: String): Int {
        val input = Files.newInputStream(Paths.get(filename))
        return withResource(input) {
            input.buffered().reader().readText().length
        }
    }

    /* This works great, however function in kotlin are first-class objects,
    hence they are implemented as classes in JVM, if the above code is used tons of times
    it will end up creating tons of objects. So its better if its just a function

    To tackle this optimization, use the work "inline". Makes sure that JVM treats them as functions
    */

    inline fun <T: AutoCloseable, U> withResourceImproved(resource: T, fn: (T) -> U): U {
        try {
            return fn(resource)
        } finally {
            resource.close()
        }
    }

    /*
    The inline modifier affects both the function itself and the lambdas passed to it:
    all of those will be inlined into the call site, so the generated code size increases
    but the efficiency advantages are huge

    If you want a function that is being passed to an inline function to not be inlined during
    code generation use the word "noinline" e.g.
    */

    inline fun <T : AutoCloseable, U, V> withResourceNew(resource: T, (T) -> U, noinline after: (U) -> V): V {
        val u = try {
            before(resource)
        } finally {
            resource.close()
        }
        return after(u)
    }


    /* Memoization: Caching calls from a function

    Lets look at a different implementation of fibonacci
    */

    fun fib(k: Int): Long = when (k) {
        0 -> 1
        1 -> 1
        else -> fib(k - 1) + fib(k - 2)
    }

    // Multiple calls will be made to the same parameters, we should cache them. EASY.

    val map = mutableMapOf<Int, Long>()
    fun memfib(k: Int): Long {
        return map.getOrPut(k) {
            when (k) {
                0 -> 1
                1 -> 1
                else -> memfib(k - 1) + memfib(k - 2)
            }
        }
    }

    // Generic memoization implementation: for function1

    fun <A, R> memoize(fn: (A) -> R): (A) -> R {
        val map = ConcurrentHashMap<A, R>()
        return { a ->
            map.getOrPut(a) {
                fn(a)
            }
        }
    }

    /* Usage
        Support "query" is a function1, which needs to be memoized.
        val memquery = memoize(::query)

    A much smart Kotlin way would be to make it a extension function on function1
    */
    fun <A, R> Function1<A, R>.memoized(): (A) -> R {
        val map = ConcurrentHashMap<A, R>()
        return {
            a -> map.getOrPut(a) {
            this.invoke(a)
        }
        }
    }

    // Usage: val memquery = ::query.memoized()

    /* TypeAlias: like Elm

    This makes the whole thing more readable, when you give types to existing simple and complex types

    Simple:
        typealias Width = Int
        typealias Length = Int
        typealias Height = Int
        fun volume(width: Width, length: Length, height: Height): Int

    Complex:
        typealias Cache = HashMap<String, Boolean>

        typealias HttpExchange = Exchange<HttpRequest, HttpResponse>
        fun process2(exchange: HttpExchange): HttpExchange

        They are simple replaced in the generated code


    Functional concepts:

    In functional language there is a concept of "Either" object. It can have two values only (Left and Right,
    where Left is Null or Error, Right is Success) and based on the value, further "fold" happens, that is, we
    call two different functions depending on the value of Either(Null/Error or Success)

    Lets implement "Either" in Kotlin

        sealed class Either<out L, out R>
        class Left<out L>(value: L): Either<L, Nothing>()
        class Right<out R>(value: R): Either<Nothing, R>()

    Lets implement fold:

        sealed class Either<out L, out R> {
            fun <T> fold(lfn: (L) -> T, rfn: (R) -> T): T = when (this) {
                is Left -> lfn(this.value)
                is Right -> rfn(this.value)
            }
        }

    Usage:

        class User(val name: String, val admin: Boolean)
        object Service Account
        class Address(val town: String, val postcode: String)

        fun getCurrentUser(): Either<ServiceAccount, User> = ... // Gets User from a service
        fun getUserAddresses(user: User): List<Address> = ...    // Gets Address from a service

        ServiceAccount is a anonymous user.

        val addresses = getCurrentUser().fold({
                emptyList<Address>()
            }, {
                getUserAddresses(it)
            }
        )

    Looks cool and simple to implement


    Projections: If you want to attach cool functional stuff like map, filter to "Either", we can do that.
    We will implement two projections, ValueProjection and EmptyProjection (noOp)

    Projection supertype:

        sealed class Projection<out T> {
            abstract fun <U> map(fn: (T) -> U): Projection<U>
            abstract fun getOrElse(or: () -> T): T
        }

        class ValueProjection<out T>(val value: T) : Projection<T>() {
            override fun <U> map(fn: (T) -> U): Projection<U> =
                ValueProjection(fn(value))
            override fun getOrElse(or: () -> T): T = value
        }
class EmptyProjection<out T> : Projection<T>() {
override fun <U> map(fn: (T) -> U): Projection<U> =
EmptyProjection<U>()
override fun getOrElse(or: () -> T): T = or()
}
fun <T> Projection<T>.getOrElse(or: () -> T): T = when (this) {
is EmptyProjection -> or()
is ValueProjection -> this.value
}



    */



}

