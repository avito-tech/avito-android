package com.avito.android.contracts.platform.output.parsers

import com.avito.android.contracts.platform.output.OutputTransformer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class CodegenJsonOutputTransformer : OutputTransformer {

    private val json = Json { ignoreUnknownKeys = true }

    override fun transform(text: String): String {
        val codegenErrors = try {
            parseErrors(text)
        } catch (e: RuntimeException) {
            return text
        }

        return codegenErrors
            .groupBy { it.scope }
            .entries
            .joinToString(separator = "\n") { entry ->
                buildString {
                    appendLine(errorsTitle(entry.key) + ":")
                    entry.value.forEach { error ->
                        appendLine(" - ${error.message}")
                    }
                }
            }
    }

    private fun parseErrors(text: String): List<CodegenError> {
        val jsons = text.split("\n")
        val codegenErrors = jsons
            .map { json.decodeFromString(CodegenError.serializer(), it) }
            .distinctBy { it.message }
        return codegenErrors
    }

    private fun errorsTitle(scope: Scope): String {
        return when (scope) {
            Scope.VALIDATION -> "Validation failures"
            Scope.GENERATION -> "Generation failures"
        }
    }
}

@Serializable
public data class CodegenError(
    @SerialName("msg") val message: String,
    @SerialName("SCOPE") val scope: Scope,
)

@Serializable
public enum class Scope {
    @SerialName("validation")
    VALIDATION,
    @SerialName("generation")
    GENERATION,
}
