package com.avito.android.module_type.restrictions.extension

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.BetweenDifferentAppsRestriction
import com.avito.android.module_type.restrictions.exclusion.BetweenDifferentAppsExclusion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class BetweenDifferentAppsRestrictionExtension @Inject constructor(
    objectFactory: ObjectFactory,
    defaultSeverity: Property<Severity>
) : BaseDependencyRestrictionExtension<BetweenDifferentAppsRestriction>(objectFactory, defaultSeverity) {

    internal abstract val commonApp: Property<ApplicationDeclaration>

    public fun appExclusion(
        app: ApplicationDeclaration,
        dependency: String,
        reason: String
    ) {
        exclusions.add(
            BetweenDifferentAppsExclusion(
                reason,
                app,
                setOf(dependency)
            )
        )
    }

    public fun appExclusion(
        app: ApplicationDeclaration,
        dependencies: Set<String>,
        reason: String
    ) {
        exclusions.add(
            BetweenDifferentAppsExclusion(
                reason,
                app,
                dependencies
            )
        )
    }

    override fun getRestriction(): BetweenDifferentAppsRestriction {
        return BetweenDifferentAppsRestriction(
            reason = reason.get(),
            commonApp = commonApp.get(),
            exclusions = exclusions.get(),
            severity = severity.get(),
        )
    }
}
