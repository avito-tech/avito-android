package com.avito.android

import com.avito.android.model.Owner
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType

public open class CodeOwnershipExtension(
    public var owners: Set<Owner> = emptySet(),
) {
    public fun checkProjectOwnershipSettings(projectPath: String) {
        if (owners.isEmpty()) {
            throwInvalidOwnershipSettingsException(projectPath)
        }
    }

    private fun throwInvalidOwnershipSettingsException(projectPath: String) {
        throw IllegalStateException(
            """
                |Owners must be set for $projectPath
                |Configure ownership settings in $projectPath `build.gradle` file. For example: 
                |
                |ownership {
                |   owners = [OwnerImpl]
                |}
            """.trimMargin()
        )
    }
}

internal val ExtensionContainer.ownership: CodeOwnershipExtension
    get() = findByType() ?: CodeOwnershipExtension()
