package com.avito.instrumentation.internal.finalizer.verdict

public object LegacyVerdictDeterminerFactory {

    public fun create(): LegacyVerdictDeterminer {
        return LegacyVerdictDeterminerImpl()
    }
}
