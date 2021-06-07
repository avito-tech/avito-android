package com.avito.runner.service

import com.avito.coroutines.extensions.Dispatchers
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.CompositeDeviceListener
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.listener.DeviceLogListener
import com.avito.runner.service.worker.listener.MessagesDeviceListener
import com.avito.time.TimeProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.File

class DeviceWorkerPoolImpl(
    private val outputDirectory: File,
    private val loggerFactory: LoggerFactory,
    private val devices: ReceiveChannel<Device>,
    private val intentionsRouter: IntentionsRouter = IntentionsRouter(loggerFactory = loggerFactory),
    private val testListener: TestListener,
    private val deviceMetricsListener: DeviceListener,
    private val timeProvider: TimeProvider,
    private val intentions: ReceiveChannel<Intention>,
    private val intentionResults: SendChannel<IntentionResult>,
    private val deviceSignals: SendChannel<Device.Signal>,
    private val deviceWorkersDispatcher: Dispatchers = Dispatchers.SingleThread
) : DeviceWorkerPool {

    private val logger = loggerFactory.create<DeviceWorkerPool>()

    private val messages: Channel<DeviceWorkerMessage> =
        Channel(Channel.UNLIMITED)

    override fun start(scope: CoroutineScope) {
        scope.launch(CoroutineName("device-workers-pool")) {
            launch(CoroutineName("device-workers")) {
                for (device in devices) {
                    DeviceWorker(
                        intentionsRouter = intentionsRouter,
                        device = device,
                        outputDirectory = outputDirectory,
                        testListener = testListener,
                        deviceListener = CompositeDeviceListener(
                            listOf(
                                MessagesDeviceListener(messages),
                                DeviceLogListener(device.logger),
                                deviceMetricsListener
                            )
                        ),
                        timeProvider = timeProvider,
                        dispatchers = deviceWorkersDispatcher
                    ).run(scope)
                }
                throw IllegalStateException("devices channel was closed")
            }

            launch(CoroutineName("send-intentions")) {
                for (intention in intentions) {
                    logger.debug("received intention: $intention")
                    intentionsRouter.sendIntention(intention = intention)
                }
            }

            launch(CoroutineName("worker-messages")) {
                for (message in messages) {
                    logger.debug("received message: $message")
                    when (message) {
                        is DeviceWorkerMessage.ApplicationInstalled ->
                            logger.debug("Application: ${message.installation.installation.application} installed")

                        is DeviceWorkerMessage.FailedIntentionProcessing -> {
                            logger.debug(
                                "Received worker failed message during executing intention:" +
                                    " ${message.intention}. Rescheduling..."
                            )

                            intentionsRouter.sendIntention(intention = message.intention)
                        }

                        is DeviceWorkerMessage.Result ->
                            intentionResults.send(message.intentionResult)

                        is DeviceWorkerMessage.WorkerDied ->
                            deviceSignals.send(Device.Signal.Died(message.coordinate))
                    }
                }
            }
        }
    }

    override fun stop() {
        intentionsRouter.cancel()
        messages.cancel()
        devices.cancel()
    }
}
