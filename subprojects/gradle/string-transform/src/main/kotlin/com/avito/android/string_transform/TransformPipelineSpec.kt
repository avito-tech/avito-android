package com.avito.android.string_transform
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import javax.inject.Inject

public abstract class TransformPipelineSpec @Inject constructor(
    private val pipelineName: String,
    objects: ObjectFactory,
) : Named {

    public val variant: Property<String> = objects.property(String::class.java)

    @get:Nested
    public abstract val rules: TransformRulesSpec

    override fun getName(): String = pipelineName

    public fun variant(value: String) {
        variant.set(value)
    }

    public fun rules(action: Action<in TransformRulesSpec>) {
        action.execute(rules)
    }
}
