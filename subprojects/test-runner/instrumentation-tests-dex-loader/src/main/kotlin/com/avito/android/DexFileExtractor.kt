package com.avito.android

import org.jf.dexlib2.iface.DexFile
import java.io.File

public interface DexFileExtractor {

    public fun getDexFiles(file: File): List<DexFile>
}
