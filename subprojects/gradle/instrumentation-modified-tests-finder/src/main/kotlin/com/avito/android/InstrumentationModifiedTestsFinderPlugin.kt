package com.avito.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class InstrumentationModifiedTestsFinderPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<ModifiedTestsFinderExtension>("modifiedTestsFinder")

        target.tasks.register<FindModifiedTestsTask>("findModifiedTests") {
            group = "impact-analysis"
        }
    }
}
