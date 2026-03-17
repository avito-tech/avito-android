package com.avito.android.string_transform

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

public abstract class TransformRulesSpec @Inject constructor(
    objects: ObjectFactory,
) {

    private val exactRules: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())

    private val caseExpandedRules: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())

    internal val exactRuleCount: Int
        get() = exactRules.get().size

    internal val caseExpandedRuleCount: Int
        get() = caseExpandedRules.get().size

    internal val totalRuleCount: Int
        get() = exactRules.get().size + caseExpandedRules.get().size

    public fun exact(from: String, to: String) {
        require(from.isNotEmpty()) {
            "transformStrings rule 'from' value must not be empty"
        }
        exactRules.add("$from->$to")
    }

    public fun caseExpanded(from: String, to: String) {
        require(from.isNotEmpty()) {
            "transformStrings rule 'from' value must not be empty"
        }
        caseExpandedRules.add("$from->$to")
    }
}
