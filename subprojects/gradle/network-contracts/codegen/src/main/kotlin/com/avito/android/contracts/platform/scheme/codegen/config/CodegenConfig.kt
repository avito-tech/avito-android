package com.avito.android.contracts.platform.scheme.codegen.config

import com.avito.android.contracts.platform.output.OutputType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.util.Base64
import kotlin.io.path.absolute

internal data class CodegenConfig(
    val name: String,
    val kind: String,
    val packageName: String,
    val apiClassName: String,
    val moduleName: String,
    val skipValidation: Boolean,
    val crtEnv: Pair<String?, Path?>,
    val keyEnv: Pair<String?, Path?>,
    val moduleDir: File,
    val schemesDirectoryRelativePath: String,
    val buildDirectoryRelativePath: String,
    val flags: Set<String>,
    val timeout: Duration,
    val errorOutputType: OutputType?,
    val mappings: Map<String, String>,
    val generators: List<String>,
)

@Suppress("unused")
@Serializable
private class CodegenConfigAdvanced(
    @SerialName("package") val packageName: String,
    @SerialName("api_class_name") val apiClassName: String,
    @SerialName("schemes_dir") val schemesDirectoryPath: String,
    @SerialName("codegen_files_dir") val buildDirectoryPath: String,
    @SerialName("module_name") val moduleName: String,
    @SerialName("flags") val flags: Set<String>,
    @SerialName("mappings") val mappings: Map<String, String>,
)

internal val CodegenConfig.args
    get(): Set<Pair<String, String?>> {
        return setOfNotNull(
            "dir" to moduleDir.path,
            "kind" to kind,
            "name" to name,
            errorOutputType?.let { "output" to it.kind },
            ("skip-validation" to null).takeIf { skipValidation }
        )
    }

internal val CodegenConfig.envVars
    get(): Set<Pair<String, String>> {
        val configJson = generateAdvancedConfigJson()
        val configBase64 = Base64.getEncoder().encodeToString(configJson.toByteArray())
        return setOfNotNull(
            crtEnv.asNonNull()?.mapValue { it.absolute().toString() },
            keyEnv.asNonNull()?.mapValue { it.absolute().toString() },
            "CODEGEN_ANDROID_CONFIG" to configBase64
        )
    }

private fun CodegenConfig.generateAdvancedConfigJson(): String {
    return Json.encodeToString<CodegenConfigAdvanced>(
        CodegenConfigAdvanced(
            packageName = this.packageName,
            apiClassName = this.apiClassName,
            schemesDirectoryPath = this.schemesDirectoryRelativePath,
            buildDirectoryPath = this.buildDirectoryRelativePath,
            moduleName = this.moduleName,
            flags = flags,
            mappings = mappings,
        )
    )
}

private fun <T : Any, V : Any> Pair<T?, V?>.asNonNull(): Pair<T, V>? {
    val first = first
    val second = second
    return if (first != null && second != null) {
        first to second
    } else {
        null
    }
}

private fun <T : Any, V : Any, R : Any> Pair<T, V>.mapValue(transform: (V) -> R): Pair<T, R> {
    return first to transform(second)
}
