package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.utils.gradle.kubernetesCredentials
import org.gradle.api.Project

internal class KubernetesConfigurator(
    private val project: Project,
    private val configuration: InstrumentationConfiguration
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.kubernetesCredentials.set(project.kubernetesCredentials)
        task.kubernetesNamespace.set(configuration.kubernetesNamespace)
    }
}
