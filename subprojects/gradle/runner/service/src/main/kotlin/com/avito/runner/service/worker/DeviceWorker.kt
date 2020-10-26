package com.avito.runner.service.worker

import com.avito.coroutines.extensions.Dispatchers
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.funktionale.tries.Try
import java.io.File

class DeviceWorker(
    private val intentionsRouter: IntentionsRouter,
    private val messagesChannel: Channel<DeviceWorkerMessage>,
    private val device: Device,
    private val outputDirectory: File,
    private val listener: TestListener,
    dispatchers: Dispatchers
) {

    private val dispatcher: CoroutineDispatcher = dispatchers.dispatcher()

    fun run(scope: CoroutineScope) = scope.launch(dispatcher) {

        var state: State = try {
            when (val status = device.deviceStatus()) {
                is Device.DeviceStatus.Alive -> device.state()
                is Device.DeviceStatus.Freeze -> {
                    // TODO MBS-8522 Will ReservationClient get a new one?
                    onDeviceDie("DeviceWorker died. Device status is `Freeze`", reason = status.reason)
                    return@launch
                }
            }
        } catch (t: Throwable) {
            onDeviceDie("DeviceWorked died. Can't get status on start", reason = t)
            return@launch
        }

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
                                onDeviceDieOnIntention(intention, preparingError)
                                return@launch
                            }
                        }
                    }
                    is Device.DeviceStatus.Freeze -> {
                        onDeviceDieOnIntention(intention, status.reason)
                        return@launch
                    }
                }
            } catch (t: Throwable) {
                onDeviceDieOnIntention(intention, t)
                return@launch
            }
        }
        device.debug("Worker ended with success result")
    }

    private suspend fun onDeviceDieOnIntention(
        intention: Intention,
        reason: Throwable
    ) {
        onDeviceDie("DeviceWorker died. Can't process intention: $intention", reason)
        messagesChannel.send(
            DeviceWorkerMessage.FailedIntentionProcessing(
                t = reason,
                intention = intention
            )
        )
    }

    private suspend fun onDeviceDie(
        message: String,
        reason: Throwable
    ) {
        device.warn(message, reason)
        messagesChannel.send(
            DeviceWorkerMessage.WorkerDied(
                t = reason,
                coordinate = device.coordinate
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
