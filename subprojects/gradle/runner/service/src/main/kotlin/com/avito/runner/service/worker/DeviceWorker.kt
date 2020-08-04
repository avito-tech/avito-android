package com.avito.runner.service.worker

import com.avito.runner.service.IntentionsRouter
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.model.intention.InstrumentationTestRunActionResult
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.model.getData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.funktionale.tries.Try
import java.io.File
import java.util.concurrent.Executors

class DeviceWorker(
    private val intentionsRouter: IntentionsRouter,
    private val messagesChannel: Channel<DeviceWorkerMessage>,
    private val device: Device,
    private val outputDirectory: File,
    private val listener: TestListener
) {

    private val scope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    fun run() = scope.launch {

        var state: State = try {
            when (val status = device.deviceStatus()) {
                is Device.DeviceStatus.Alive -> device.state()
                is Device.DeviceStatus.Freeze -> {
                    // No intention is lost. DeviceWorkerMessage.WorkerFailed event is unnecessary.
                    // Can't use this device any more. TODO Will ReservationClient get a new one?
                    device.warn("Device wasn't booted. Failed on initial run", status.reason)
                    return@launch
                }
            }
        } catch (t: Throwable) {
            throw RuntimeException("Unexpected error when initialize a $device", t)
        }

        // what will happen when worker dies
        for (intention in intentionsRouter.observeIntentions(state)) {
            try {
                device.debug("Receive intention: $intention")
                when (val status = device.deviceStatus()) {
                    is Device.DeviceStatus.Alive -> {
                        device.debug("Preparing state: ${intention.state}")
                        val (preparingError, newState) = prepareDeviceState(
                            currentState = state,
                            intentionState = intention.state
                        ).toEither()
                        when {
                            newState != null -> {
                                device.debug("State prepared")
                                state = newState
                                val result = executeAction(action = intention.action)
                                device.debug("Worker test run completed for intention")
                                messagesChannel.send(
                                    DeviceWorkerMessage.Result(
                                        intentionResult = IntentionResult(
                                            intention = intention,
                                            actionResult = InstrumentationTestRunActionResult(
                                                testCaseRun = result
                                            )
                                        )
                                    )
                                )
                            }
                            preparingError != null -> {
                                onDeviceDie(intention, preparingError)
                                return@launch
                            }
                        }
                    }
                    is Device.DeviceStatus.Freeze -> {
                        onDeviceDie(intention, status.reason)
                        return@launch
                    }
                }
            } catch (t: Throwable) {
                throw RuntimeException("Unexpected error while process intention: $intention", t)
            }
        }
        device.debug("Worker ended with success result")
    }

    /**
     * DeviceWorker is die. TODO release device
     */
    private suspend fun onDeviceDie(
        intention: Intention,
        reason: Throwable
    ) {
        device.warn("DeviceWorker died. Can't process intention: $intention", reason)

        messagesChannel.send(
            DeviceWorkerMessage.WorkerFailed(
                t = reason,
                intention = intention
            )
        )
    }

    private suspend fun prepareDeviceState(
        currentState: State,
        intentionState: State
    ): Try<State> {
        device.debug("Checking device state. Current: ${currentState.digest}, desired: ${intentionState.digest}")
        if (intentionState.digest == currentState.digest) {
            clearStatePackages(currentState).get()
            return Try.Success(intentionState)
        }

        try {
            intentionState.layers
                .forEach {
                    when (it) {
                        is State.Layer.InstalledApplication -> {
                            val installation = device.installApplication(
                                application = it.applicationPath
                            )
                            messagesChannel.send(
                                DeviceWorkerMessage.ApplicationInstalled(
                                    installation
                                )
                            )
                        }
                    }
                }
        } catch (t: Throwable) {
            return Try.Failure(t)
        }

        return Try.Success(intentionState)
    }

    private fun executeAction(action: InstrumentationTestRunAction): DeviceTestCaseRun {
        val deviceTestCaseRun = try {
            listener.started(
                device = device,
                targetPackage = action.targetPackage,
                test = action.test,
                executionNumber = action.executionNumber
            )
            device.runIsolatedTest(
                action = action,
                outputDir = outputDirectory
            )
        } catch (t: Throwable) {
            val now = System.currentTimeMillis()

            DeviceTestCaseRun(
                testCaseRun = TestCaseRun(
                    test = action.test,
                    result = TestCaseRun.Result.Failed.InfrastructureError(
                        errorMessage = "Unexpected infrastructure error: ${t.message}",
                        cause = t
                    ),
                    timestampStartedMilliseconds = now,
                    timestampCompletedMilliseconds = now
                ),
                device = device.getData()
            )
        }

        listener.finished(
            device = device,
            test = action.test,
            targetPackage = action.targetPackage,
            result = deviceTestCaseRun.testCaseRun.result,
            durationMilliseconds = deviceTestCaseRun.testCaseRun.durationMilliseconds,
            executionNumber = action.executionNumber
        )

        return deviceTestCaseRun
    }

    private fun clearStatePackages(state: State): Try<Any> = Try {
        device.debug("Clearing packages")
        state.layers
            .asSequence()
            .filterIsInstance<State.Layer.InstalledApplication>()
            .forEach { device.clearPackage(it.applicationPackage) }
    }

    private fun Device.state(): State =
        State(
            layers = listOf(
                State.Layer.ApiLevel(api = api),
                State.Layer.Model(model = model)
            )
        )
}
