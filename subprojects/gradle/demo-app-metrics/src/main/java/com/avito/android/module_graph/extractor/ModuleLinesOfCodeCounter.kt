package com.avito.android.module_graph.extractor

import com.avito.android.module_graph.models.ClocOutput
import com.avito.utils.ProcessRunner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Duration

public interface ModuleLinesOfCodeCounter {

    public fun count(projectDir: File, moduleName: String, sourceSetDirName: String): Int
}

public class ModuleLinesOfCodeCounterImpl(
    private val defaultJson: Json,
) : ModuleLinesOfCodeCounter {

    public override fun count(projectDir: File, moduleName: String, sourceSetDirName: String): Int {
        val modulePath = moduleName.drop(1).replace(':', '/')
        if (!projectDir.resolve("$modulePath/src/$sourceSetDirName").exists()) {
            return 0
        }
        val clockResult = ProcessRunner
            .create(projectDir)
            .run(command = "cloc $modulePath/src/$sourceSetDirName --json", Duration.ofSeconds(10))
            .getOrThrow()

        val output = defaultJson.decodeFromString<ClocOutput>(clockResult)
        return output.kotlin.code + output.xml.code
    }
}
