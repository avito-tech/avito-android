package com.avito.android.contracts.platform.extension.configurations.validation

import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.NetworkContractsDiagnosticRule
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class ValidationRulesGroup(private val objects: ObjectFactory) : Named {

    public val rules: NamedDomainObjectContainer<NetworkContractsDiagnosticRule> get() = diagnosticRules

    public val onlyIf: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)

    public abstract val dependsOn: SetProperty<String>

    private val diagnosticRules: ExtensiblePolymorphicDomainObjectContainer<NetworkContractsDiagnosticRule> = objects
        .polymorphicDomainObjectContainer(NetworkContractsDiagnosticRule::class.java)

    private val registeredTypes: MutableSet<Class<out NetworkContractsDiagnosticRule>> = mutableSetOf()

    public fun <T : NetworkContractsDiagnosticRule> registerRule(
        name: String,
        type: Class<T>,
        configuration: Action<T>
    ) {
        if (!registeredTypes.contains(type)) {
            diagnosticRules.registerFactory(type) { objects.newInstance(type, it) }
            registeredTypes.add(type)
        }
        diagnosticRules.register(name, type, configuration)
    }
}
