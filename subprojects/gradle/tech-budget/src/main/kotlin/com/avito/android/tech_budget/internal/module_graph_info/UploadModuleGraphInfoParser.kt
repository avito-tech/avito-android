package com.avito.android.tech_budget.internal.module_graph_info

import com.avito.android.module_graph.models.ModuleGraphInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

public class UploadModuleGraphInfoParser {

    private val defaultJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    public fun parseModuleGraphInfoFromFile(file: File): ModuleGraphInfo {
        require(file.exists()) {
            "module-graph.json file doesn't exist"
        }
        return defaultJson.decodeFromString<ModuleGraphInfo>(file.readText())
    }
}
