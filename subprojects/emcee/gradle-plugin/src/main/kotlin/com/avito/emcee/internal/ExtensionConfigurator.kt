package com.avito.emcee.internal

import com.avito.emcee.EmceeExtension
import com.avito.emcee.EmceeTestTask

internal class ExtensionConfigurator(
    private val emceeExtension: EmceeExtension
) : EmceeTestTaskConfigurator {

    override fun configure(task: EmceeTestTask) {
        task.job.set(emceeExtension.job)
        task.retries.set(emceeExtension.retries)
        task.devices.set(emceeExtension.devices)
        task.testTimeout.set(emceeExtension.testTimeout)
        task.baseUrl.set(emceeExtension.queueBaseUrl)
        task.configTestMode.set(emceeExtension.configTestMode)
        task.outputDir.set(emceeExtension.outputDir)
        task.artifactory.set(emceeExtension.artifactory)
    }
}
