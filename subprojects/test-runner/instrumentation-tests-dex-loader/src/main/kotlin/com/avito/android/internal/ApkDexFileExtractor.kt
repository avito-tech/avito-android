package com.avito.android.internal

import com.avito.android.DexFileExtractor
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.DexFile
import java.io.File

public class ApkDexFileExtractor : DexFileExtractor {

    override fun getDexFiles(file: File): List<DexFile> {
        val dexEntryNames = DexFileFactory.loadDexContainer(file, null).dexEntryNames
        return dexEntryNames.map { DexFileFactory.loadDexEntry(file, it, true, null).dexFile }
    }
}
