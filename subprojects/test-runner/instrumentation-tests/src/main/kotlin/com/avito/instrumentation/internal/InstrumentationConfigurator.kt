package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.StaticDeviceReservationConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.InstrumentationParameters
import com.avito.runner.config.Reservation
import com.avito.runner.config.SchedulingConfigurationData
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.scheduler.suite.filter.Filter
import com.avito.test.model.DeviceName
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

internal class InstrumentationConfigurator(
    private val extension: InstrumentationTestsPluginExtension,
    private val configuration: InstrumentationConfiguration,
    private val instrumentationArgsResolver: InstrumentationArgsResolver,
    private val outputDir: Provider<Directory>,
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {

        val instrumentationParameters = instrumentationArgsResolver.getInstrumentationArgsForTestTask()

        task.instrumentationConfiguration.set(
            getInstrumentationConfiguration(
                configuration = configuration,
                parentInstrumentationParameters = instrumentationParameters,
                filters = extension.filters.map { getInstrumentationFilter(it) },
                outputFolder = outputDir.get().asFile,
            )
        )

        task.suppressFailure.set(configuration.suppressFailure)
        task.suppressFlaky.set(configuration.suppressFlaky)
    }

    private fun getInstrumentationConfiguration(
        configuration: InstrumentationConfiguration,
        parentInstrumentationParameters: InstrumentationParameters,
        filters: List<InstrumentationFilterData>,
        outputFolder: File,
    ): InstrumentationConfigurationData {

        val mergedInstrumentationParameters: InstrumentationParameters =
            parentInstrumentationParameters
                .applyParameters(configuration.instrumentationParams)

        return InstrumentationConfigurationData(
            name = configuration.name,
            instrumentationParams = mergedInstrumentationParameters,
            reportSkippedTests = configuration.reportSkippedTests,
            targets = getTargets(configuration, mergedInstrumentationParameters),
            testRunnerExecutionTimeout = configuration.testRunnerExecutionTimeout,
            instrumentationTaskTimeout = configuration.instrumentationTaskTimeout,
            filter = filters.singleOrNull { it.name == configuration.filter }
                ?: throw IllegalStateException("Can't find filter=${configuration.filter}"),
            outputFolder = outputFolder
        )
    }

    private fun validate(targetConfiguration: TargetConfiguration) {
        validate(targetConfiguration.scheduling)
    }

    private fun validate(schedulingConfiguration: SchedulingConfiguration) {
        schedulingConfiguration.reservation
        schedulingConfiguration.reservation.validate()
        schedulingConfiguration.quota
        schedulingConfiguration.quota.validate()
    }

    private fun getTargets(
        configuration: InstrumentationConfiguration,
        parentInstrumentationParameters: InstrumentationParameters
    ): List<TargetConfigurationData> {
        val result = configuration.targetsContainer.toList()
            .filter { it.enabled }
            .map { getTargetConfiguration(it, parentInstrumentationParameters) }

        require(result.isNotEmpty()) {
            "configuration ${configuration.name} must have at least one target"
        }

        return result
    }

    private fun getTargetConfiguration(
        targetConfiguration: TargetConfiguration,
        parentInstrumentationParameters: InstrumentationParameters
    ): TargetConfigurationData {

        validate(targetConfiguration)

        val deviceName = targetConfiguration.deviceName

        require(deviceName.isNotBlank()) { "target.deviceName should be set" }

        return TargetConfigurationData(
            name = targetConfiguration.name,
            reservation = getScheduling(targetConfiguration.scheduling).reservation,
            deviceName = DeviceName(deviceName),
            instrumentationParams = parentInstrumentationParameters
                .applyParameters(targetConfiguration.instrumentationParams)
                .applyParameters(
                    mapOf("deviceName" to deviceName)
                )
        )
    }

    private fun getScheduling(schedulingConfiguration: SchedulingConfiguration): SchedulingConfigurationData {
        val currentReservation = schedulingConfiguration.reservation

        return SchedulingConfigurationData(
            reservation = when (currentReservation) {
                is StaticDeviceReservationConfiguration -> Reservation.StaticReservation(
                    device = currentReservation.device,
                    count = currentReservation.count,
                    quota = schedulingConfiguration.quota.data()
                )
                is TestsBasedDevicesReservationConfiguration -> Reservation.TestsCountBasedReservation(
                    device = currentReservation.device,
                    quota = schedulingConfiguration.quota.data(),
                    testsPerEmulator = currentReservation.testsPerEmulator!!,
                    maximum = currentReservation.maximum!!,
                    minimum = currentReservation.minimum
                )
                else -> throw RuntimeException("Unknown type of reservation")
            }
        )
    }

    private fun getInstrumentationFilter(instrumentationFilter: InstrumentationFilter): InstrumentationFilterData {
        return InstrumentationFilterData(
            name = instrumentationFilter.name,
            fromSource = getFromSource(instrumentationFilter.fromSource),
            fromRunHistory = getFromRunHistory(instrumentationFilter.fromRunHistory)
        )
    }

    private fun getFromSource(fromSource: InstrumentationFilter.FromSource): InstrumentationFilterData.FromSource {
        return InstrumentationFilterData.FromSource(
            prefixes = fromSource.prefixes.value,
            annotations = fromSource.annotations.value,
            excludeFlaky = fromSource.excludeFlaky
        )
    }

    private fun getFromRunHistory(
        fromRunHistory: InstrumentationFilter.FromRunHistory
    ): InstrumentationFilterData.FromRunHistory {
        return InstrumentationFilterData.FromRunHistory(
            previousStatuses = Filter.Value(
                included = fromRunHistory.previous.value.included.map { it.map() }.toSet(),
                excluded = fromRunHistory.previous.value.excluded.map { it.map() }.toSet()
            ),
            reportFilter = fromRunHistory.reportFilter?.let { filter ->
                InstrumentationFilterData.FromRunHistory.ReportFilter(
                    statuses = Filter.Value(
                        included = filter.statuses.value.included.map { it.map() }.toSet(),
                        excluded = filter.statuses.value.excluded.map { it.map() }.toSet()
                    )
                )
            }
        )
    }

    private fun InstrumentationFilter.FromRunHistory.RunStatus.map(): com.avito.runner.config.RunStatus {
        return when (this) {
            InstrumentationFilter.FromRunHistory.RunStatus.Failed -> com.avito.runner.config.RunStatus.Failed
            InstrumentationFilter.FromRunHistory.RunStatus.Success -> com.avito.runner.config.RunStatus.Success
            InstrumentationFilter.FromRunHistory.RunStatus.Lost -> com.avito.runner.config.RunStatus.Lost
            InstrumentationFilter.FromRunHistory.RunStatus.Skipped -> com.avito.runner.config.RunStatus.Skipped
            InstrumentationFilter.FromRunHistory.RunStatus.Manual -> com.avito.runner.config.RunStatus.Manual
        }
    }
}
