package com.avito.android.gradle_configuration.extension

import com.avito.android.gradle_configuration.extension.spec.NupokatiPipelineSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV2PipelineSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV4PipelineSpec
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import javax.inject.Inject

public abstract class NupokatiExtension @Inject constructor(objects: ObjectFactory) {

    internal val pipelines: ExtensiblePolymorphicDomainObjectContainer<NupokatiPipelineSpec> =
        objects.polymorphicDomainObjectContainer(
            NupokatiPipelineSpec::class.java
        )

    init {
        pipelines.registerBinding(NupokatiV2PipelineSpec::class.java, NupokatiV2PipelineSpec::class.java)
        pipelines.registerBinding(NupokatiV4PipelineSpec::class.java, NupokatiV4PipelineSpec::class.java)
    }

    public fun v2(
        name: String,
        action: Action<NupokatiV2PipelineSpec>,
    ): NamedDomainObjectProvider<NupokatiV2PipelineSpec> {
        return try {
            pipelines.named(name, NupokatiV2PipelineSpec::class.java).apply {
                configure(action)
            }
        } catch (_: UnknownDomainObjectException) {
            pipelines.register(name, NupokatiV2PipelineSpec::class.java, action)
        }
    }

    public fun v4(
        name: String,
        action: Action<NupokatiV4PipelineSpec>,
    ): NamedDomainObjectProvider<NupokatiV4PipelineSpec> {
        return try {
            pipelines.named(name, NupokatiV4PipelineSpec::class.java).apply {
                configure(action)
            }
        } catch (_: UnknownDomainObjectException) {
            pipelines.register(name, NupokatiV4PipelineSpec::class.java, action)
        }
    }

    public fun releaseVersion(specName: String): Provider<String> {
        return pipelines.named(specName).flatMap { it.releaseVersion() }
    }
}
