package com.avito.android

import com.avito.android.model.Owner
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType

public open class CodeOwnershipExtension {

    public var owners: Set<Owner> = emptySet()
    public var emptyOwnersErrorMessage: String = DEFAULT_EMPTY_OWNERS_ERROR_MESSAGE

    public fun owners(vararg owners: Owner) {
        this.owners = owners.toSet()
    }

    internal fun checkProjectOwnershipSettings(projectPath: String) {
        if (owners.isEmpty()) {
            throwInvalidOwnershipSettingsException(projectPath)
        }
    }

    private fun throwInvalidOwnershipSettingsException(projectPath: String) {
        throw IllegalStateException(emptyOwnersErrorMessage.format(projectPath))
    }

    private companion object {
        val DEFAULT_EMPTY_OWNERS_ERROR_MESSAGE =
            """
                |Owners must be set for the %s project.
                |Configure the ownership extension for this project in the buildscript: 
                |
                |ownership {
                |   owners(Owner1, Owner2)
                |}
            """.trimMargin()
    }
}

public val ExtensionContainer.ownership: CodeOwnershipExtension
    get() = findByType() ?: CodeOwnershipExtension()
