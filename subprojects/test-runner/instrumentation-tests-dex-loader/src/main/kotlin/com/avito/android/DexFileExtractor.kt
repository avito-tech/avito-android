package com.avito.android

import org.jf.dexlib2.iface.DexFile
import java.io.File

interface DexFileExtractor {

    fun getDexFiles(file: File): List<DexFile>
}
