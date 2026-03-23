package com.avito.android.string_transform.internal.report

internal class FakeMonotonicTimeSource(
    private val incrementNanos: Long,
) : MonotonicTimeSource {
    private var currentNanos: Long = 0

    override fun nowNanos(): Long {
        val value = currentNanos
        currentNanos += incrementNanos
        return value
    }
}
