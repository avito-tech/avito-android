package com.avito.android.network_contracts.codegen.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path
import java.util.Base64
import kotlin.io.path.absolute

internal data class CodegenConfig(
    val name: String,
    val kind: String,
    val packageName: String,
    val moduleName: String,
    val skipValidation: Boolean,
    val crtEnv: Pair<String, Path>,
    val keyEnv: Pair<String, Path>,
    val moduleDir: File,
    val schemesDirectoryRelativePath: String,
    val buildDirectoryRelativePath: String,
)

@Suppress("unused")
@Serializable
private class CodegenConfigAdvanced(
    @SerialName("package") val packageName: String,
    @SerialName("schemes_dir") val schemesDirectoryPath: String,
    @SerialName("codegen_files_dir") val buildDirectoryPath: String,
    @SerialName("module_name") val moduleName: String,
)

internal val CodegenConfig.args
    get(): Set<Pair<String, String?>> {
        return setOfNotNull(
            "dir" to moduleDir.path,
            "kind" to kind,
            "name" to name,
            ("skip-validation" to null).takeIf { skipValidation }
        )
    }

internal val CodegenConfig.envVars
    get(): Set<Pair<String, String>> {
        val configJson = generateAdvancedConfigJson()
        val configBase64 = Base64.getEncoder().encodeToString(configJson.toByteArray())
        return setOf(
            crtEnv.first to crtEnv.second.absolute().toString(),
            keyEnv.first to keyEnv.second.absolute().toString(),
            "CODEGEN_ANDROID_CONFIG" to configBase64
        )
    }

private fun CodegenConfig.generateAdvancedConfigJson(): String {
    return Json.encodeToString<CodegenConfigAdvanced>(
        CodegenConfigAdvanced(
            packageName = this.packageName,
            schemesDirectoryPath = this.schemesDirectoryRelativePath,
            buildDirectoryPath = this.buildDirectoryRelativePath,
            moduleName = this.moduleName,
        )
    )
}
