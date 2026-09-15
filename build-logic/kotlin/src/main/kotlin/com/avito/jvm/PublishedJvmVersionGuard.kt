package com.avito.jvm

import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata

internal fun Project.registerPublishedJvmVersionGuard(expectedJvmVersion: String) {
    plugins.withId("maven-publish") {
        val metadataTasks = tasks.withType(GenerateModuleMetadata::class.java)

        val check = tasks.register("checkPublishedJvmVersion", CheckPublishedJvmVersionTask::class.java) { task ->
            task.group = "verification"
            task.description =
                "Checks that published Gradle module metadata advertises org.gradle.jvm.version=$expectedJvmVersion"
            task.expectedJvmVersion.set(expectedJvmVersion)
            task.moduleFiles.from(metadataTasks.map { it.outputFile })
        }

        tasks.withType(AbstractPublishToMaven::class.java).configureEach { publish ->
            publish.dependsOn(check)
        }
    }
}
