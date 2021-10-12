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
