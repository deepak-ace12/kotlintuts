/**
 * Created by rane on 16/7/17.
 */


/*
Generics : Classed with type parameters
*/

class Box<T>(t: T) {
    var value = t
}

val box1: Box<Int> = Box<Int>(1)        // explicit
val box2 = Box(1)                       // inferred

/*
    Example, "List" supports type parameters (In Java or other languages)

    List<Object> is the most generic type but is not the super-type of List<String>
    Why? Because if list1 is of type List<String> then casting it to List<Object> will
    let you add integers to the list.

    // Java
    List<String> strs = new ArrayList<String>();
    List<Object> objs = strs; // !!! The cause of the upcoming problem sits here. Java prohibits this!
    objs.add(1); // Here we put an Integer into a list of Strings
    String s = strs.get(0); // !!! ClassCastException: Cannot cast Integer to String

    If a generic interface has both methods that return and methods that consume
    objects of type T then it should be "invariant". Examples are: List<T>, Set<T>

    However, addAll which has the following signature

    // Java
    interface Collection<E> ... {
        void addAll(Collection<E> items);
    }

    is a problem, since you can add "String" to a List<Object>
    but not add an "Object" to List<String>

    To Solve this the addAll signature is:

    // Java
    interface Collection<E> ... {
      void addAll(Collection<? extends E> items);
    }

    which implies, that addAll will take any class which extends type E
    this works, so you can add Strings to a List<Objects> but not vice-versa

    This is CoVariance: In terms of a interface,if a generic interface has
    only methods that return objects of type T, but don’t consume objects
    of type T, then assignment from a variable of Type<B> to a variable
    of Type<A> can make sense.
    Examples are: Iterable<T>, Iterator<T>, Supplier<T>

    A definition
        void myFunc(Collection<? super E> items);
    myFunc will take any class which is a super class of E

    This is ContraVariance: In terms of a interface, If a generic interface
    has only methods that consume objects of type T, but don’t return
    objects of type T, then assignment from a variable of Type<A> to a variable
    of Type<B> can make sense.
    Examples are: Comparable<T>, Consumer<T>

    The key to understanding why this trick works is rather simple:
    if you can only TAKE items from a collection, then using a collection of Strings
    and reading Objects from it is fine. Conversely, if you can only PUT items into
    the collection, it's OK to take a collection of Objects and put Strings into it

    This is called as PECS, stands for Producer-Extends, Consumer-Super

    Using PECS philosophy, Kotlin supports (not possible in Java)
    Declaration-site variance and Use-site variance

    Declaration-site variance: We can annotate the type parameter T of Source to
    make sure that it is only returned (produced) from members of Source<T>, and
    never consumed. To do this we provide the "out" modifier:
*/

    abstract class Source<out T> {
        abstract fun nextT(): T         // Source can only hold methods which return T
    }                                   // not which take T as input

    /*
    Usage:

    fun demo(strs: Source<String>) {
        val objects: Source<Any> = strs // This is OK, since T is an out-parameter
        // ...
    }
    */

    abstract class Comparable<in T> {
        abstract fun compareTo(other: T): Int
    }

    /*
    Usage

    fun demo(x: Comparable<Number>) {
        x.compareTo(1.0) // 1.0 has type Double, which is a subtype of Number
        // Thus, we can assign x to a variable of type Comparable<Double>
        val y: Comparable<Double> = x // OK!
    }


    Use-site variance
        For a lot of cases, its impossible to define types which only have
        "in" or "out". For such invariant cases, kotlin provides "type projections"
    */

    fun copy(from: Array<out Any>, to: Array<Any>) {
        // ...
    }

    /*
        "from" is not simply an array, but a restricted(projected one), you can
        only call "get" on it (can only read from the "from" array)
        It can be any derived class of Any
        Similarly
    */

    fun fill(dest: Array<in String>, value: String) {
        // ...
    }

    /*
        "dest" has to be a String or a parent class of String
    */

/*
Advanced concepts:

Anonymous classes using object expression and object declarations

*/


    /*
    Initialization-on-demand holder pattern (or lazy loading Singleton)

        Often times you want a Singleton to load only when you need it (heavy loading class)
        To accomplish this in Java, we declare a static class inside the Singleton class, which holds the
        instance of the Singleton. The getInstance method of the class will return this static class holder

            public class ClassWithHeavyInitialization {

                private ClassWithHeavyInitialization() {   // Private constructor, nobody can instantiate outside
                }

                private static class LazyHolder {          // Static class having access to outer class private methods
                    // holder of the outer class instance
                    public static final ClassWithHeavyInitialization INSTANCE = new ClassWithHeavyInitialization();
                }

                public static ClassWithHeavyInitialization getInstance() {
                    return LazyHolder.INSTANCE;
                }
            }

        How this helps in lazy loading, is that until you call getInstance,
        the Singleton instance is never created.

    Is there a better way? Kotlin provides the following:
    */

    public class ClassWithHeavyInitialization private constructor() {
        private object Holder {
            val INSTANCE = ClassWithHeavyInitialization()
        }

        companion object {
            val instance: ClassWithHeavyInitialization by lazy {
                Holder.INSTANCE
            }
        }
    }

/*
Factory design pattern

*/