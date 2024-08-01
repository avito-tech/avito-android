package com.avito.android.module_type.restrictions.extension

import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.ToWiringRestriction
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class ToWiringRestrictionExtension @Inject constructor(
    objectFactory: ObjectFactory,
    defaultSeverity: Property<Severity>
) : BaseDependencyRestrictionExtension<ToWiringRestriction>(objectFactory, defaultSeverity) {

    override fun getRestriction(): ToWiringRestriction {
        return ToWiringRestriction(
            reason = reason.get(),
            exclusions = exclusions.get(),
            severity = severity.get()
        )
    }
}
