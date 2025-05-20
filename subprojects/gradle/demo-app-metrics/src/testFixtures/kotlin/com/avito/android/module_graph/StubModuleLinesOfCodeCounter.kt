package com.avito.android.module_graph

import com.avito.android.module_graph.extractor.ModuleLinesOfCodeCounter
import java.io.File

object StubModuleLinesOfCodeCounter : ModuleLinesOfCodeCounter {
    override fun count(
        projectDir: File,
        moduleName: String,
        sourceSetDirName: String
    ): Int {
        return if (sourceSetDirName == "test" || sourceSetDirName == "androidTest") {
            1
        } else {
            10
        }
    }
}
