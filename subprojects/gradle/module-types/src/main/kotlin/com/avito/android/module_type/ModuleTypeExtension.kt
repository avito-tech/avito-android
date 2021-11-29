package com.avito.android.module_type

import org.gradle.api.provider.Property

public abstract class ModuleTypeExtension {

    public abstract val type: Property<ModuleType>

    internal fun ensureHasType(projectPath: String) {
        check(type.isPresent) {
            """
            |Module type must be set for the $projectPath project.
            |Configure an extension in the buildscript: 
            |
            |module {
            |   type.set(...)
            |}
            """.trimMargin()
        }
    }
}
