package com.avito.android.string_transform.internal.report

internal object SystemMonotonicTimeSource : MonotonicTimeSource {

    override fun nowNanos(): Long = System.nanoTime()
}
