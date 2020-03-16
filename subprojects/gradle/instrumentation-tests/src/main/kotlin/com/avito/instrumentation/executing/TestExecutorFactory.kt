package com.avito.instrumentation.executing

import com.avito.instrumentation.report.listener.TestReporter
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger

interface TestExecutorFactory {

    fun createExecutor(
        logger: CILogger,
        kubernetesCredentials: KubernetesCredentials,
        buildId: String,
        buildType: String,
        projectName: String,
        testReporter: TestReporter?,
        registry: String
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            logger: CILogger,
            kubernetesCredentials: KubernetesCredentials,
            buildId: String,
            buildType: String,
            projectName: String,
            testReporter: TestReporter?,
            registry: String
        ): TestExecutor {
            return KubernetesTestExecutor(
                logger = logger,
                kubernetesCredentials = kubernetesCredentials,
                buildId = buildId,
                buildType = buildType,
                projectName = projectName,
                testReporter = testReporter,
                registry = registry
            )
        }
    }
}
