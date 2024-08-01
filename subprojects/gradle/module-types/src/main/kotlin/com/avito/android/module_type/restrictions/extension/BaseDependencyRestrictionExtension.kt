package com.avito.android.module_type.restrictions.extension

import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.DependencyRestriction
import com.avito.android.module_type.restrictions.exclusion.BetweenModulesExclusion
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class BaseDependencyRestrictionExtension<T : DependencyRestriction>(
    objectFactory: ObjectFactory,
    defaultSeverity: Property<Severity>,
) {

    internal abstract val reason: Property<String>
    internal abstract val exclusions: ListProperty<DependencyRestrictionExclusion>
    public val severity: Property<Severity> = objectFactory.property<Severity>().convention(defaultSeverity)

    public fun modulesExclusion(
        module: String,
        dependency: String,
        reason: String,
    ) {
        exclusions.add(BetweenModulesExclusion(setOf(module), setOf(dependency), reason))
    }

    public fun modulesExclusion(
        modules: Set<String>,
        dependency: String,
        reason: String,
    ) {
        exclusions.add(BetweenModulesExclusion(modules, setOf(dependency), reason))
    }

    public fun modulesExclusion(
        module: String,
        dependencies: Set<String>,
        reason: String,
    ) {
        exclusions.add(BetweenModulesExclusion(setOf(module), dependencies, reason))
    }

    internal abstract fun getRestriction(): T
}
