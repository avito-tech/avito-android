package com.avito.instrumentation.suite.dex

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.DexFile
import java.io.File

interface DexFileExtractor {

    fun getDexFiles(file: File): List<DexFile>
}

class ApkDexFileExtractor : DexFileExtractor {

    override fun getDexFiles(file: File): List<DexFile> {
        val dexEntryNames = DexFileFactory.loadDexContainer(file, null).dexEntryNames
        return dexEntryNames.map { DexFileFactory.loadDexEntry(file, it, true, null).dexFile }
    }
}
