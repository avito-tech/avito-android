package com.avito.android.string_transform

import com.avito.android.string_transform.internal.validation.TransformPipelinesValidator
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public abstract class TransformStringsExtension @Inject constructor(
    private val projectPath: String,
    objects: ObjectFactory,
) {
    internal val pipelines: NamedDomainObjectContainer<TransformPipelineSpec> =
        objects.domainObjectContainer(TransformPipelineSpec::class.java) { name ->
            objects.newInstance(TransformPipelineSpec::class.java, name)
        }

    public fun create(
        name: String,
        action: Action<in TransformPipelineSpec>,
    ): TransformPipelineSpec {
        return pipelines.create(name) { pipeline ->
            action.execute(pipeline)
            TransformPipelinesValidator.validate(projectPath, pipeline)
        }
    }
}
