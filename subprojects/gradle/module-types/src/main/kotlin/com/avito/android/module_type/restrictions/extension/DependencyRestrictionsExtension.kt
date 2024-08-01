package com.avito.android.module_type.restrictions.extension

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class DependencyRestrictionsExtension @Inject constructor(
    private val objects: ObjectFactory,
) {
    public val defaultSeverity: Property<Severity> = objects.property<Severity>().convention(Severity.fail)
    public abstract val solutionMessage: Property<String>
    internal abstract val betweenFunctionalTypesRestrictions: ListProperty<BetweenFunctionalTypesExtension>
    internal abstract val betweenDifferentAppsRestriction: Property<BetweenDifferentAppsRestrictionExtension>
    internal abstract val toWiringRestriction: Property<ToWiringRestrictionExtension>

    public fun betweenFunctionalTypes(
        fromType: FunctionalType,
        allowedTypes: Map<ConfigurationType, Set<FunctionalType>>,
        reason: String,
        action: Action<BetweenFunctionalTypesExtension> = Action {}
    ) {
        val restriction = objects.newInstance(BetweenFunctionalTypesExtension::class.java, defaultSeverity)
        betweenFunctionalTypesRestrictions.add(restriction)
        restriction.fromType.set(fromType)
        restriction.allowedTypes.set(allowedTypes)
        restriction.reason.set(reason)
        action.execute(restriction)
    }

    public fun betweenDifferentApps(
        reason: String,
        commonApp: ApplicationDeclaration,
        action: Action<BetweenDifferentAppsRestrictionExtension> = Action {}
    ) {
        val restriction = objects.newInstance(BetweenDifferentAppsRestrictionExtension::class.java, defaultSeverity)
        restriction.commonApp.set(commonApp)
        restriction.reason.set(reason)
        action.execute(restriction)
        betweenDifferentAppsRestriction.set(restriction)
        betweenDifferentAppsRestriction.disallowChanges()
    }

    public fun toWiring(
        reason: String,
        action: Action<ToWiringRestrictionExtension> = Action {}
    ) {
        val restriction = objects.newInstance(ToWiringRestrictionExtension::class.java, defaultSeverity)
        restriction.reason.set(reason)
        action.execute(restriction)
        toWiringRestriction.set(restriction)
        toWiringRestriction.disallowChanges()
    }
}
