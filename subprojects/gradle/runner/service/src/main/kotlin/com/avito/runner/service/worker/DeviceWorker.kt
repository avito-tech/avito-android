package com.avito.runner.service.worker

import com.avito.runner.service.IntentionsRouter
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.model.intention.InstrumentationTestRunActionResult
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
            checkDeviceAlive()
            device.state()
        } catch (t: Exception) {
            device.warn("Error while checking device initial state", t)
            // No intention is lost. DeviceWorkerMessage.WorkerFailed event is unnecessary.
            // Can't use this device any more. ReservationClient will get a new one.
            return@launch
        }

        for (intention in intentionsRouter.observeIntentions(state)) {
            try {
                checkDeviceAlive()

                device.log("Receive intention: $intention")

                device.log("Preparing state: ${intention.state}")
                state = prepareDeviceState(currentState = state, intentionState = intention.state).get()
                device.log("State prepared")

                val result = executeAction(action = intention.action)
                device.log("Worker test run completed for intention")
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

            } catch (t: Throwable) {
                // means device worker is die. TODO release device
                device.warn("Error during $intention processing", t)

                messagesChannel.send(
                    DeviceWorkerMessage.WorkerFailed(
                        t = t,
                        intention = intention
                    )
                )

                return@launch
            }
        }

        device.log("Worker ended with success result")
    }

    private suspend fun prepareDeviceState(
        currentState: State,
        intentionState: State
    ): Try<State> {
        device.log("Checking device state. Current: ${currentState.digest}, desired: ${intentionState.digest}")
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
        device.log("Clearing packages")
        state.layers
            .asSequence()
            .filterIsInstance<State.Layer.InstalledApplication>()
            .forEach { device.clearPackage(it.applicationPackage) }
    }

    private fun checkDeviceAlive() {
        device.log("Getting device status")
        val status = device.deviceStatus()
        device.log("Device status: $status")

        if (status is Device.DeviceStatus.Freeze) {
            throw RuntimeException("Device: ${device.id} not answered for a long time")
        }
    }

    private fun Device.state(): State =
        State(
            layers = listOf(
                State.Layer.ApiLevel(api = api),
                State.Layer.Model(model = model)
            )
        )
}
