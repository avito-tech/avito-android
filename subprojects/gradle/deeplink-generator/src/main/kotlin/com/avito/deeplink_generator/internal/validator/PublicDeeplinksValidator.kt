package com.avito.deeplink_generator.internal.validator

import com.avito.deeplink_generator.model.Deeplink

/**
 * Validates that deeplinks which are marked as public in code, also presented in build script, and vice versa.
 */
internal object PublicDeeplinksValidator {

    @Throws(RuntimeException::class)
    fun validate(
        publicDeeplinksFromCode: Set<Deeplink>,
        publicDeeplinksFromBuildScript: Set<Deeplink>,
        codeFixHint: String = "",
    ) {
        if (publicDeeplinksFromBuildScript.isEmpty() && publicDeeplinksFromCode.isEmpty()) {
            return
        }

        val validDeeplinks = mutableListOf<Deeplink>()
        val codeOnlyDeeplinks = mutableListOf<Deeplink>()

        publicDeeplinksFromCode.forEach { link ->
            if (publicDeeplinksFromBuildScript.contains(link)) {
                validDeeplinks.add(link)
            } else {
                codeOnlyDeeplinks.add(link)
            }
        }

        if (codeOnlyDeeplinks.isNotEmpty()) {
            error(
                """
                    Deeplinks are marked as public in code, but not in build script.
                    Such deeplinks: $codeOnlyDeeplinks 
                    Please, add code snippet below to this module's build.gradle file:
                    deeplinkGenerator { 
                        publicDeeplinks(
                            ${codeOnlyDeeplinks.joinToString(separator = ",\n") { "\"$it\"" }}
                        )
                    }
                """.trimIndent()
            )
        }
        val buildScriptOnlyDeeplinks = publicDeeplinksFromBuildScript - validDeeplinks
        if (buildScriptOnlyDeeplinks.isNotEmpty()) {
            error(
                """
                    Deeplinks are marked as public in buildScript, but not in a code.
                    Such deeplinks: $buildScriptOnlyDeeplinks
                    $codeFixHint
                """.trimIndent()
            )
        }
    }
}
