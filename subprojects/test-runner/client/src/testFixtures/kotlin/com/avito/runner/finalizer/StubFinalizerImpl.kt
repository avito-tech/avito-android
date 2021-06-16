package com.avito.runner.finalizer

import com.avito.runner.finalizer.action.FinalizeAction
import com.avito.runner.finalizer.verdict.VerdictDeterminer
import com.avito.runner.finalizer.verdict.VerdictDeterminerImpl
import com.avito.runner.finalizer.verdict.createStubInstance
import java.io.File

internal fun FinalizerImpl.Companion.createStubInstance(
    actions: List<FinalizeAction> = emptyList(),
    verdictFile: File = File.createTempFile("verdict", ".json"),
    verdictDeterminer: VerdictDeterminer = VerdictDeterminerImpl.createStubInstance(),
    finalizerFileDumper: FinalizerFileDumper = StubFinalizerFileDumper()
): FinalizerImpl {
    return FinalizerImpl(
        actions = actions,
        verdictFile = verdictFile,
        verdictDeterminer = verdictDeterminer,
        finalizerFileDumper = finalizerFileDumper,
    )
}
