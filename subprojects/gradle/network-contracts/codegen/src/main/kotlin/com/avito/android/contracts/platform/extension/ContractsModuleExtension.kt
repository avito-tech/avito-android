package com.avito.android.contracts.platform.extension

import com.avito.android.contracts.platform.extension.configurations.codegen.CodegenConfiguration
import com.avito.android.contracts.platform.extension.configurations.import.ImportConfiguration
import com.avito.android.contracts.platform.extension.configurations.validation.ValidationConfiguration
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

public abstract class ContractsModuleExtension(
    objects: ObjectFactory,
) : ExtensionAware {

    public abstract val projectName: Property<String>

    public abstract val kind: Property<String>

    public val schemesBaseDirectory: DirectoryProperty = objects.directoryProperty()

    public val codegen: CodegenConfiguration =
        objects.newInstance(CodegenConfiguration::class.java, objects)

    public val imports: NamedDomainObjectContainer<ImportConfiguration> =
        objects.domainObjectContainer(ImportConfiguration::class.java)

    public val validations: NamedDomainObjectContainer<ValidationConfiguration> =
        objects.domainObjectContainer(ValidationConfiguration::class.java)

    internal companion object {
        internal const val NAME = "contracts"
    }
}

public fun ContractsModuleExtension.codegen(action: Action<CodegenConfiguration>) {
    action.execute(codegen)
}
