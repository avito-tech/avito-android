package com.avito.report.model

public sealed class TestStatus {

    public object Success : TestStatus() {
        override fun toString(): String = "Success"
    }

    public data class Failure(val verdict: String) : TestStatus() {
        override fun toString(): String = "Failure"
    }

    public data class Skipped(val reason: String) : TestStatus() {
        override fun toString(): String = "Skipped"
    }

    public object Manual : TestStatus() {
        override fun toString(): String = "Manual"
    }

    public object Lost : TestStatus() {
        override fun toString(): String = "Lost"
    }
}
