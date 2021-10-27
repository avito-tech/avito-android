package com.avito.android

import com.avito.android.model.Owner
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType

public open class CodeOwnershipExtension(
    public var owners: Set<Owner> = emptySet(),
) {

    public fun owners(vararg owners: Owner) {
        this.owners = owners.toSet()
    }

    internal fun checkProjectOwnershipSettings(projectPath: String) {
        if (owners.isEmpty()) {
            throwInvalidOwnershipSettingsException(projectPath)
        }
    }

    private fun throwInvalidOwnershipSettingsException(projectPath: String) {
        throw IllegalStateException(
            """
                |Owners must be set for the $projectPath project.
                |Configure the ownership extension for $projectPath in the buildscript: 
                |
                |ownership {
                |   owners(Owner1, Owner2)
                |}
            """.trimMargin()
        )
    }
}

internal val ExtensionContainer.ownership: CodeOwnershipExtension
    get() = findByType() ?: CodeOwnershipExtension()
