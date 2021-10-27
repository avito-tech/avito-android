@file:Suppress("deprecation")
package com.avito.android

import Visibility
import com.avito.android.model.Owner
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType

public open class CodeOwnershipExtension(
    public var owners: Set<Owner> = emptySet(),
    @Deprecated("Modules visibility restriction is deprecated. Use `owners` property instead")
    public var team: String? = null,
    @Deprecated("Modules visibility restriction is deprecated.")
    public var visibility: Visibility = Visibility.PUBLIC,
    @Deprecated("Modules visibility restriction is deprecated.")
    public var allowedDependencies: Set<String> = emptySet()
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
