package com.avito.runner.service

import com.avito.runner.logging.Logger
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.observer.DevicesObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File

interface IntentionExecutionService {
    fun start(): Communication
    fun stop()

    class Communication(
        val intentions: SendChannel<Intention>,
        val results: ReceiveChannel<IntentionResult>
    )
}

class IntentionExecutionServiceImplementation(
    private val outputDirectory: File,
    private val logger: Logger,
    private val devicesObserver: DevicesObserver,
    private val intentionsRouter: IntentionsRouter = IntentionsRouter(),
    private val listener: TestListener? = null
) : IntentionExecutionService {

    private val intentions: Channel<Intention> =
        Channel(Channel.UNLIMITED)
    private val results: Channel<IntentionResult> =
        Channel(Channel.UNLIMITED)
    private val messages: Channel<DeviceWorkerMessage> =
        Channel(Channel.UNLIMITED)

    override fun start(): IntentionExecutionService.Communication {

        GlobalScope.launch {
            for (device in devicesObserver.observeDevices()) {
                DeviceWorker(
                    intentionsRouter = intentionsRouter,
                    device = device,
                    outputDirectory = outputDirectory,
                    messagesChannel = messages,
                    listener = listener
                ).run()
            }
        }

        GlobalScope.launch {
            for (intention in intentions) {
                intentionsRouter.sendIntention(intention = intention)
            }
        }

        GlobalScope.launch {
            for (message in messages) {
                when (message) {
                    is DeviceWorkerMessage.ApplicationInstalled -> {
                        logger.log("Application: ${message.installation.installation.application} installed")
                    }
                    is DeviceWorkerMessage.WorkerFailed -> {
                        logger.log(
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

        return IntentionExecutionService.Communication(
            intentions = intentions,
            results = results
        )
    }

    override fun stop() {
        intentionsRouter.close()
        intentions.close()
        results.close()
    }
}
