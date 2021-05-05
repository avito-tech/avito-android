package com.avito.android.build_trace.internal

@Suppress("ClassName")
internal sealed class TaskResult {

    object EXECUTED : TaskResult()

    class FAILED(val error: Throwable?) : TaskResult() {

        override fun toString(): String {
            return "${this.javaClass.simpleName} $error" // TODO: extract a clean stacktrace
        }
    }

    object UP_TO_DATE : TaskResult()

    class SKIPPED(val skipMessage: String) : TaskResult() {

        override fun toString(): String {
            return if (skipMessage == "SKIPPED") {
                this.javaClass.simpleName
            } else {
                "${this.javaClass.simpleName} $skipMessage"
            }
        }
    }

    object NO_SOURCE : TaskResult()

    object UNKNOWN : TaskResult()

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}
