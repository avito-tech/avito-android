package com.avito.android.contracts.platform.extension.configurations.codegen

import com.avito.android.contracts.platform.output.OutputType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class CodegenConfiguration @Inject constructor(objects: ObjectFactory) {

    public abstract val packageName: Property<String>

    public abstract val apiClassName: Property<String>

    public abstract val version: Property<String>

    public val flags: SetProperty<String> = objects.setProperty(String::class.java)

    public val mappings: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
        .convention(emptyMap())

    public val skipValidation: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public val clearBeforeRun: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    public val codegenTimeoutSeconds: Property<Long> = objects.property<Long>()
        .convention(20)

    public val generatedDirectory: DirectoryProperty = objects.directoryProperty()

    public val errorOutputType: Property<OutputType> = objects.property<OutputType>()

    public val generators: ListProperty<String> = objects.listProperty()

    public val outputTransformers: NamedDomainObjectContainer<OutputTransformerConfiguration> = objects
        .domainObjectContainer(OutputTransformerConfiguration::class.java)
}
