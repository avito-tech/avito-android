package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import kotlin.metadata.jvm.KotlinModuleMetadata
import kotlin.metadata.jvm.UnstableMetadataApi

@OptIn(UnstableMetadataApi::class)
internal class KotlinModuleTransformer {

    fun transform(
        inputFile: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> = Result.tryCatch {
        val originalBytes = inputFile.readBytes()
        val metadata = KotlinModuleMetadata.read(originalBytes)
            ?: error("Unsupported kotlin_module format: ${inputFile.path}")

        val module = metadata.kmModule
        var changed = false

        val rewrittenPackageParts = LinkedHashMap<String, kotlin.metadata.jvm.KmPackageParts>(module.packageParts.size)
        module.packageParts.forEach { (fqn, parts) ->
            val newFqn = applyRules(fqn, rules)
            if (newFqn != fqn) changed = true

            parts.fileFacades.replaceAll { facade ->
                val newFacade = applyRules(facade, rules)
                if (newFacade != facade) changed = true
                newFacade
            }

            if (parts.multiFileClassParts.isNotEmpty()) {
                val rewrittenMfcp = LinkedHashMap<String, String>(parts.multiFileClassParts.size)
                parts.multiFileClassParts.forEach { (key, value) ->
                    val newKey = applyRules(key, rules)
                    val newValue = applyRules(value, rules)
                    if (newKey != key || newValue != value) changed = true
                    rewrittenMfcp[newKey] = newValue
                }
                parts.multiFileClassParts.clear()
                parts.multiFileClassParts.putAll(rewrittenMfcp)
            }

            rewrittenPackageParts[newFqn] = parts
        }
        if (changed) {
            module.packageParts.clear()
            module.packageParts.putAll(rewrittenPackageParts)
            inputFile.writeBytes(metadata.write())
        }
    }

    private fun applyRules(value: String, rules: List<NormalizedRule>): String =
        rules.fold(value) { current, rule -> current.replace(rule.from, rule.to) }
}
