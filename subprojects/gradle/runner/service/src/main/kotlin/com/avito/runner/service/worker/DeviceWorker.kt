package com.avito.runner.service.worker

import com.avito.runner.service.IntentionsRouter
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.ActionResult
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
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

    private var state: State = device.state()

    fun run() = scope.launch {

        for (intention in intentionsRouter.observeIntentions(state)) {
            try {
                checkDeviceAlive()

                device.log("Receive intention: $intention")

                device.log("Preparing state: ${intention.state}")
                state = prepareState(intentionState = intention.state).get()
                device.log("State prepared")

                val result = executeAction(action = intention.action)
                device.log("Worker test run completed for intention")
                messagesChannel.send(
                    DeviceWorkerMessage.Result(
                        intentionResult = IntentionResult(
                            intention = intention,
                            actionResult = ActionResult.InstrumentationTestRunActionResult(
                                testCaseRun = result
                            )
                        )
                    )
                )

            } catch (t: Throwable) {
                device.log("Something went wrong during intention executing: ${t.message}")

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

    /**
     * Готовим состояние девайса к прогну теста
     */
    private suspend fun prepareState(intentionState: State): Try<State> {
        device.log("Check worker state. Current: ${state.digest}, desired: ${intentionState.digest}")
        if (intentionState.digest == state.digest) {
            clearStatePackages().get()
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

    private fun executeAction(action: InstrumentationTestRunAction): DeviceTestCaseRun =
        try {
            device.runIsolatedTest(
                action = action,
                outputDir = outputDirectory,
                listener = listener
            )
        } catch (t: Throwable) {
            val now = System.currentTimeMillis()

            DeviceTestCaseRun(
                testCaseRun = TestCaseRun(
                    test = action.test,
                    result = TestCaseRun.Result.Failed(
                        stacktrace = "Infrastructure error: ${t.message}"
                    ),
                    timestampStartedMilliseconds = now,
                    timestampCompletedMilliseconds = now
                ),
                device = device.getData()
            )
        }

    private fun clearStatePackages(): Try<Any> = Try {
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
                State.Layer.ApiLevel(api = api)
            )
        )
}
