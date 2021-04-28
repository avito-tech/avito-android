package com.avito.instrumentation.internal.finalizer.verdict

internal object LegacyVerdictDeterminerFactory {

    fun create(): LegacyVerdictDeterminer {
        return LegacyVerdictDeterminerImpl()
    }
}
