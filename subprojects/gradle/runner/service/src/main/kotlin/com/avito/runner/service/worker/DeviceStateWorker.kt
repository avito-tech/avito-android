package com.avito.runner.service.worker

import com.avito.android.Result
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.model.intention.State.Layer.InstalledApplication
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation

internal class DeviceStateWorker(private val device: Device) {

    inline fun installApplications(
        state: State,
        onAllSucceeded: (List<DeviceInstallation>) -> Unit
    ): Result<Unit> = state.layers
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

    fun clearPackages(state: State): Result<Unit> = state.layers
        .asSequence()
        .filterIsInstance<InstalledApplication>()
        .map {
            device.clearPackage(it.applicationPackage)
        }
        .reduce { _, unusedValue -> unusedValue }

    private inline fun <T, R> List<Result<T>>.aggregate(
        onAllSucceeded: (List<T>) -> Unit,
        errorsAggregator: (List<Throwable>) -> Throwable,
        resultAggregator: (List<T>) -> R
    ): Result<R> =
        if (all { it is Result.Success }) {
            val results = map { it.getOrThrow() }
            onAllSucceeded(results)
            Result.Success(resultAggregator(results))
        } else {
            val errors = filterIsInstance<Result.Failure<T>>().map { it.throwable }
            Result.Failure(errorsAggregator(errors))
        }
}
