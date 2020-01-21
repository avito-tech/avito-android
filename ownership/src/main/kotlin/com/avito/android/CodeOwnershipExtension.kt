package com.avito.android

import Visibility
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.findByType

open class CodeOwnershipExtension(
    var team: String? = null,
    var visibility: Visibility = Visibility.PUBLIC,
    var allowedDependencies: Set<String> = emptySet()
) {

    fun checkProjectOwnershipSettings(projectPath: String) {
        if ((team == null && visibility == Visibility.PRIVATE) ||
            (team == null && visibility == Visibility.TEAM)
        ) {
            throwInvalidOwnershipSettingsException(projectPath, visibility)
        }
    }

    private fun throwInvalidOwnershipSettingsException(projectPath: String, visibility: Visibility) {
        throw IllegalStateException(
            "Team must be set for $visibility modules. \n" +
                "Configure ownership settings in $projectPath `build.gradle` file. For example: \n" +
                "\n" +
                "ownership {\n" +
                "    team 'MY_TEAM_NAME'\n" +
                "    visibility ${Visibility::class.java.simpleName}.$visibility\n" +
                "}"
        )
    }
}

internal val Project.ownership: CodeOwnershipExtension
    get() = extensions.findByType() ?: error("ownership should be available for $path")

val ExtensionContainer.ownership: CodeOwnershipExtension
    get() = findByType() ?: error("ownership should be available")
