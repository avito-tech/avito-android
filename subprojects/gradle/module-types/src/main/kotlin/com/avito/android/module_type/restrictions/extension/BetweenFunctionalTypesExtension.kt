package com.avito.android.module_type.restrictions.extension

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.BetweenFunctionalTypesRestriction
import com.avito.module.configurations.ConfigurationType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class BetweenFunctionalTypesExtension @Inject constructor(
    objectFactory: ObjectFactory,
    defaultSeverity: Property<Severity>
) : BaseDependencyRestrictionExtension<BetweenFunctionalTypesRestriction>(objectFactory, defaultSeverity) {

    internal abstract val fromType: Property<FunctionalType>
    internal abstract val allowedTypes: MapProperty<ConfigurationType, Set<FunctionalType>>

    override fun getRestriction(): BetweenFunctionalTypesRestriction {
        return BetweenFunctionalTypesRestriction(
            fromType = fromType.get(),
            allowedTypesByConfiguration = allowedTypes.get(),
            reason = reason.get(),
            exclusions = exclusions.get(),
            severity = severity.get(),
        )
    }
}
