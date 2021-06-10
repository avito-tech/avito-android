package com.avito.runner.finalizer.verdict

internal object LegacyVerdictDeterminerFactory {

    fun create(): LegacyVerdictDeterminer {
        return LegacyVerdictDeterminerImpl()
    }
}
