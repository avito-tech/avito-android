package com.avito.android

import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

@Deprecated(
    "This plugin will be removed in the next infra release, MBSA-974",
    ReplaceWith("com.avito.android.code-ownership")
)
public class CodeOwnershipValidationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val codeOwnershipExtension = target.extensions.create<CodeOwnershipExtension>("ownership")
        val strictOwnership = target.getBooleanProperty("avito.ownership.strictOwnership", false)
        if (!strictOwnership) return

        target.afterEvaluate {
            if (it.state.failure == null && codeOwnershipExtension.owners.isEmpty()) {
                throw IllegalStateException(codeOwnershipExtension.emptyOwnersErrorMessage.format(it.path))
            }
        }
    }
}
