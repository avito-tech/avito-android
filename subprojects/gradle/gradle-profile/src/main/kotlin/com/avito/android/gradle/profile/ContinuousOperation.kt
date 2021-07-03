package com.avito.android.gradle.profile

public open class ContinuousOperation(override val description: String) : Operation() {

    public var startTime: Long = 0
        private set

    public var finish: Long = 0
        private set

    override val elapsedTime: Long
        get() = this.finish - this.startTime

    override fun toString(): String {
        return this.description
    }

    public fun setStart(start: Long): ContinuousOperation {
        this.startTime = start
        return this
    }

    public fun setFinish(finish: Long): ContinuousOperation {
        this.finish = finish
        return this
    }
}
