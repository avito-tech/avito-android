package com.avito.android.module_type.restrictions

import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.module.configurations.ConfigurationType

fun DependencyRestriction(
    reason: String = "Stub reason",
    severity: Severity = Severity.fail,
    isRestricted: (ModuleWithType, ModuleWithType, ConfigurationType) -> Boolean = { _, _, _ -> true }
) = object : DependencyRestriction(emptyList()) {
    override val reason: String = reason

    override val severity: Severity = severity

    override fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        return isRestricted(module, dependency, configuration)
    }
}
