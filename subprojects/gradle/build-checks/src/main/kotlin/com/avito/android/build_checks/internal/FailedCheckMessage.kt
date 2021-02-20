package com.avito.android.build_checks.internal

import com.avito.android.build_checks.BuildChecksExtension
import com.avito.android.build_checks.extensionName
import kotlin.reflect.KFunction

internal class FailedCheckMessage(
    /**
     * Reference to the [BuildChecksExtension] method
     */
    extensionMethodReference: KFunction<Any>,
    private val message: String
) {

    private val checkExtensionName = extensionMethodReference.name

    override fun toString() =
        """
ERROR: '$checkExtensionName' build check is failed.

${message.trimIndent()}

This check can be disabled in an extension:
$extensionName {
    $checkExtensionName {
        enabled = false
    }
}
See https://avito-tech.github.io/avito-android/projects/BuildChecks (search '$checkExtensionName')
""".trimIndent()
}
