package com.avito.android.string_transform.internal.report

internal fun interface MonotonicTimeSource {

    fun nowNanos(): Long
}
