package com.avito.android.network_contracts

import com.avito.android.contracts.platform.extension.configurations.codegen.OutputTransformerConfiguration
import com.avito.android.contracts.platform.output.OutputType
import com.avito.android.contracts.platform.scheme.imports.data.ParamType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

public abstract class NetworkContractsModuleExtension(
    objects: ObjectFactory,
) : ExtensionAware {

    public abstract val kind: Property<String>

    public abstract val projectName: Property<String>

    public abstract val packageName: Property<String>

    public abstract val apiClassName: Property<String>

    public abstract val version: Property<String>

    @Deprecated("Unused. Fail fast is default strategy")
    public val failFast: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public val flags: SetProperty<String> = objects.setProperty(String::class.java)

    public val mappings: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
        .convention(emptyMap())

    public val skipValidation: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public val codegenTimeoutSeconds: Property<Long> = objects.property<Long>()
        .convention(20)

    public val schemesDirName: Property<String> = objects.property<String>()
        .convention("api-clients")

    public val apiSchemesDirectory: DirectoryProperty = objects.directoryProperty()

    public val generatedDirectory: DirectoryProperty = objects.directoryProperty()

    public val errorOutputType: Property<OutputType> = objects.property<OutputType>()

    public val outputTransformers: NamedDomainObjectContainer<OutputTransformerConfiguration> = objects
        .domainObjectContainer(OutputTransformerConfiguration::class.java)

    public val validationByCodegen: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    @Deprecated("Use importTypedParameters instead")
    public val importParameters: MapProperty<String, String> = objects.mapProperty()

    public val importTypedParameters: MapProperty<String, ParamType> = objects.mapProperty()

    internal companion object {
        internal const val NAME = "networkContracts"
    }
}
