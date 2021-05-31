package com.avito.instrumentation.internal.logcat

internal object StubLogcatAccessor : LogcatAccessor {

    override val isAvailable: Boolean = false

    override fun getLogs(): Logcat = Logcat.STUB
}
