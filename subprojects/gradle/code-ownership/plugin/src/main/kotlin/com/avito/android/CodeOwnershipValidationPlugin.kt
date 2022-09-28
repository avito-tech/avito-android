package com.avito.android

import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class CodeOwnershipValidationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val codeOwnershipExtension = target.extensions.create<CodeOwnershipExtension>("ownership")
        val strictOwnership = target.getBooleanProperty("avito.ownership.strictOwnership", false)

        target.afterEvaluate {
            if (strictOwnership) {
                codeOwnershipExtension.checkProjectOwnershipSettings(it.path)
            }
        }
    }
}
