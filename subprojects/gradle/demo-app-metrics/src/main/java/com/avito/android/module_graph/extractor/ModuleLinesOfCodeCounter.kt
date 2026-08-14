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
        val processRunner = ProcessRunner.create(projectDir)
        val timeout = Duration.ofSeconds(10)
        val clockResult = processRunner
            .run(command = "cloc $modulePath/src/$sourceSetDirName --json", timeout)
            .getOrElse { cause ->
                processRunner.run(command = "cloc --version", timeout).getOrElse {
                    throw IllegalStateException(
                        "cloc not found. Install it and make it available on PATH, " +
                            "for example: brew install cloc",
                        cause,
                    )
                }
                throw cause
            }

        val output = defaultJson.decodeFromString<ClocOutput>(clockResult)
        return output.kotlin.code + output.xml.code
    }
}
