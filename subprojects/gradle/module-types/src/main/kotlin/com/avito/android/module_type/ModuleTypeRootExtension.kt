package com.avito.android.module_type

import com.avito.android.module_type.restrictions.extension.DependencyRestrictionsExtension
import org.gradle.api.Action
import org.gradle.api.tasks.Nested

public abstract class ModuleTypeRootExtension {

    @get:Nested
    internal abstract val dependencyRestrictionsExtension: DependencyRestrictionsExtension

    public fun dependencyRestrictions(action: Action<DependencyRestrictionsExtension>) {
        action.execute(dependencyRestrictionsExtension)
    }

    internal companion object {
        internal const val name = "moduleTypes"
    }
}
