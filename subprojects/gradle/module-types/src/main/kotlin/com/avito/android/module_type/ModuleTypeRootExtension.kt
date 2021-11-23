package com.avito.android.module_type

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public abstract class ModuleTypeRootExtension {

    public abstract val severity: Property<Severity>

    public abstract val restrictions: ListProperty<DependencyRestriction>

    internal companion object {
        internal const val name = "moduleTypes"
    }
}
