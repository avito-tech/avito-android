package com.avito.instrumentation.internal.logcat

internal interface LogcatAccessor {

    val isAvailable: Boolean

    /**
     * Logcat will be populated with empty lists if not available;
     * Check [isAvailable] to avoid useless work
     */
    fun getLogs(): Logcat
}
