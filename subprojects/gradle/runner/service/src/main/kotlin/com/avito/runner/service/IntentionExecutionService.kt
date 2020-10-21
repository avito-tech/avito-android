package com.avito.runner.service

import com.avito.logger.Logger
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File

interface IntentionExecutionService {
    fun start(scope: CoroutineScope): Communication
    fun stop()

    class Communication(
        val intentions: SendChannel<Intention>,
        val results: ReceiveChannel<IntentionResult>
    )
}

class IntentionExecutionServiceImplementation(
    private val outputDirectory: File,
    private val logger: Logger,
    private val devices: ReceiveChannel<Device>,
    private val intentionsRouter: IntentionsRouter = IntentionsRouter(),
    private val listener: TestListener
) : IntentionExecutionService {

    private val intentions: Channel<Intention> =
        Channel(Channel.UNLIMITED)
    private val results: Channel<IntentionResult> =
        Channel(Channel.UNLIMITED)
    private val messages: Channel<DeviceWorkerMessage> =
        Channel(Channel.UNLIMITED)

    override fun start(scope: CoroutineScope): IntentionExecutionService.Communication {
        scope.launch {
            launch {
                for (device in devices) {
                    DeviceWorker(
                        intentionsRouter = intentionsRouter,
                        device = device,
                        outputDirectory = outputDirectory,
                        messagesChannel = messages,
                        listener = listener
                    ).run(scope)
                }
            }

            launch {
                for (intention in intentions) {
                    intentionsRouter.sendIntention(intention = intention)
                }
            }

            launch {
                for (message in messages) {
                    when (message) {
                        is DeviceWorkerMessage.ApplicationInstalled -> {
                            logger.debug("Application: ${message.installation.installation.application} installed")
                        }
                        is DeviceWorkerMessage.WorkerFailed -> {
                            logger.debug(
                                "Received worker failed message during executing intention:" +
                                    " ${message.intention}. Rescheduling..."
                            )

                            intentionsRouter.sendIntention(intention = message.intention)
                        }
                        is DeviceWorkerMessage.Result -> {
                            results.send(message.intentionResult)
                        }
                    }
                }
            }
        }

        return IntentionExecutionService.Communication(
            intentions = intentions,
            results = results
        )
    }

    override fun stop() {
        intentionsRouter.close()
        intentions.close()
        results.close()
        messages.close()
        devices.cancel()
    }
}
