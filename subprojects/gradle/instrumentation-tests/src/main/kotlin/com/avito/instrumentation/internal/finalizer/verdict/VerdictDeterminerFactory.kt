package com.avito.instrumentation.internal.finalizer.verdict

internal object VerdictDeterminerFactory {

    fun create(): VerdictDeterminer {
        return VerdictDeterminerImpl()
    }
}
