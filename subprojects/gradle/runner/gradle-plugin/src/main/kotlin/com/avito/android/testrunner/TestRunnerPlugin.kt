package com.avito.android.testrunner

import com.avito.android.testrunner.service.TestService
import org.gradle.api.Plugin
import org.gradle.api.Project

public class TestRunnerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<TestRunnerExtensions>("testRunner")

        @Suppress("UnstableApiUsage")
        val serviceProvider = target.gradle.sharedServices.registerIfAbsent(
            "testService",
            TestService::class.java
        ) { spec ->
            spec.parameters.kubernetesCredentials.set(extension.kubernetesCredentials)
        }

        target.tasks.register<TestTask>("testRun") {

            service.set(serviceProvider)
        }
    }
}
