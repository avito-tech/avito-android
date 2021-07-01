package com.avito.runner.service.worker.device

public sealed class Serial {

    public abstract val value: String

    public data class Remote(override val value: String) : Serial() {
        override fun toString(): String {
            return value
        }
    }

    public data class Local(override val value: String) : Serial() {
        override fun toString(): String {
            return value
        }
    }
}
