package com.avito.android.plugin.build_param_check

import kotlin.reflect.KFunction

internal class FailedCheckMessage(
    /**
     * Reference to the [BuildChecksExtension] method
     */
    extensionMethodReference: KFunction<Any>,
    private val message: String
) {
    override fun toString() =
"""
ERROR: '$checkExtensionName' build check is failed.
${message.trimIndent()}
This check can be disabled by extension:
$extensionName {
    $checkExtensionName {
        enabled = false
    }
}
See https://avito-tech.github.io/avito-android/docs/projects/buildchecks/ (search '$checkExtensionName')
""".trimIndent()

    private val checkExtensionName = extensionMethodReference.name
}
