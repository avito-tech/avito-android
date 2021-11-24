package com.avito.android.gradle.profile

public class CompositeOperation<T : Operation>(children: Iterable<T>) : Operation(), Iterable<T> {

    public val operations: List<T>

    override val elapsedTime: Long
        get() {
            var sum = 0L

            var child: Operation
            val var3 = this.operations.iterator()
            while (var3.hasNext()) {
                child = var3.next()
                sum += child.elapsedTime
            }

            return sum
        }

    override val description: String
        get() = "<composite operation>"

    init {
        this.operations = children.toMutableList()
    }

    override fun iterator(): Iterator<T> {
        return this.operations.iterator()
    }
}
