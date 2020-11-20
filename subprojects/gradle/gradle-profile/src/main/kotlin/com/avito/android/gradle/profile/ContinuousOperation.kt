package com.avito.android.gradle.profile

open class ContinuousOperation(override val description: String) : Operation() {

    var startTime: Long = 0
        private set

    private var finish: Long = 0

    override val elapsedTime: Long
        get() = this.finish - this.startTime

    override fun toString(): String {
        return this.description
    }

    fun setStart(start: Long): ContinuousOperation {
        this.startTime = start
        return this
    }

    fun setFinish(finish: Long): ContinuousOperation {
        this.finish = finish
        return this
    }
}
