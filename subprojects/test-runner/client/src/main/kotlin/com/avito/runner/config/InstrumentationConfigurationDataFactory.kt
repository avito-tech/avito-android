package com.avito.runner.config

import com.avito.test.model.DeviceName

public class InstrumentationConfigurationDataFactory(
    private val instrumentationConfigurationCacheableData: InstrumentationConfigurationCacheableData,
    private val mergedInstrumentationParams: InstrumentationParameters,
    private val reportConfig: RunnerReportConfig,
    private val targetInstrumentationParams: Map<DeviceName, InstrumentationParameters>,
) {

    public fun create(): InstrumentationConfigurationData {
        return InstrumentationConfigurationData(
            name = instrumentationConfigurationCacheableData.name,
            instrumentationParams = mergedInstrumentationParams,
            reportSkippedTests = instrumentationConfigurationCacheableData.reportSkippedTests,
            targets = getTargets(instrumentationConfigurationCacheableData, targetInstrumentationParams),
            testRunnerExecutionTimeout = instrumentationConfigurationCacheableData.testRunnerExecutionTimeout,
            instrumentationTaskTimeout = instrumentationConfigurationCacheableData.instrumentationTaskTimeout,
            singleTestRunTimeout = instrumentationConfigurationCacheableData.singleTestRunTimeout,
            filter = instrumentationConfigurationCacheableData.filter,
            reportConfig = reportConfig,
        )
    }

    private fun getTargets(
        instrumentationConfigurationCacheableData: InstrumentationConfigurationCacheableData,
        targetInstrumentationParams: Map<DeviceName, InstrumentationParameters>,
    ): List<TargetConfigurationData> {
        return instrumentationConfigurationCacheableData.targets
            .map { cacheableTarget: TargetConfigurationCacheableData ->
                TargetConfigurationData(
                    name = cacheableTarget.name,
                    reservation = cacheableTarget.reservation,
                    deviceName = cacheableTarget.deviceName,
                    instrumentationParams = checkNotNull(targetInstrumentationParams[cacheableTarget.deviceName])
                )
            }
    }
}
