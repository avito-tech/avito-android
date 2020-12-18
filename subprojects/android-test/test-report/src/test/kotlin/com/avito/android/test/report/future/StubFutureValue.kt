package com.avito.android.test.report.future

import com.avito.filestorage.FutureValue

class StubFutureValue<T>(
    private val value: T
) : FutureValue<T> {

    override fun get(): T = value
}
