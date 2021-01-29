package com.avito.runner.service.worker

import com.avito.runner.service.model.intention.State
import com.avito.runner.service.model.intention.State.Layer.InstalledApplication
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation
import org.funktionale.tries.Try

internal class DeviceStateWorker(private val device: Device) {

    inline fun installApplications(
        state: State,
        onAllSucceeded: (List<DeviceInstallation>) -> Unit
    ): Try<Unit> = state.layers
        .filterIsInstance<InstalledApplication>()
        .map {
            device.installApplication(applicationPackage = it.applicationPath)
        }
        .aggregate(
            onAllSucceeded = onAllSucceeded,
            errorsAggregator = { errors ->
                // that's not a full info about installations problems, but should be enough for most cases
                IllegalStateException("Some application installations was not successful", errors.first())
            },
            resultAggregator = {}
        )

    fun clearPackages(state: State): Try<Any> = state.layers
        .asSequence()
        .filterIsInstance<InstalledApplication>()
        .map {
            device.clearPackage(it.applicationPackage)
        }
        .reduce { _, unusedValue -> unusedValue }

    private inline fun <T, R> List<Try<T>>.aggregate(
        onAllSucceeded: (List<T>) -> Unit,
        errorsAggregator: (List<Throwable>) -> Throwable,
        resultAggregator: (List<T>) -> R
    ): Try<R> =
        if (all { it.isSuccess() }) {
            val results = map { it.get() }
            onAllSucceeded(results)
            Try.Success(resultAggregator(results))
        } else {
            val errors = filterIsInstance<Try.Failure<T>>().map { it.throwable }
            Try.Failure(errorsAggregator(errors))
        }
}
