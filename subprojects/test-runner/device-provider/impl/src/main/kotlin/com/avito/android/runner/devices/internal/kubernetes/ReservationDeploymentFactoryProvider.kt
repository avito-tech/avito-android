package com.avito.android.runner.devices.internal.kubernetes

import com.avito.logger.LoggerFactory

public class ReservationDeploymentFactoryProvider(
    private val configurationName: String,
    private val projectName: String,
    private val buildId: String,
    private val buildType: String,
    private val loggerFactory: LoggerFactory
) {
    internal fun provide(): ReservationDeploymentFactory {
        return ReservationDeploymentFactoryImpl(
            configurationName = configurationName,
            projectName = projectName,
            buildId = buildId,
            buildType = buildType,
            deploymentNameGenerator = UUIDDeploymentNameGenerator(),
            loggerFactory = loggerFactory
        )
    }
}
