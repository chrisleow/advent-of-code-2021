/**
 * Interfaces
 */

interface ImmutableList<T> : List<T> {
    fun addFirst(other: T): ImmutableList<T>
    fun addFirst(other: Iterable<T>): ImmutableList<T>
    fun addLast(other: T): ImmutableList<T>
    fun addLast(other: Iterable<T>): ImmutableList<T>
}

