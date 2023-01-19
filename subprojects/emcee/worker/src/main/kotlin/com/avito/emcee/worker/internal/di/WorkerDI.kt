package com.avito.emcee.worker.internal.di

import com.avito.android.device.avd.AvdConfig
import com.avito.android.device.avd.internal.AvdConfigurationProvider
import com.avito.android.device.manager.AndroidDeviceManagerFactory
import com.avito.emcee.worker.Config
import com.avito.emcee.worker.WorkerQueueApi.Companion.createWorkerQueueApi
import com.avito.emcee.worker.configuration.PayloadSignature
import com.avito.emcee.worker.internal.SingleInstanceAndroidDeviceTestExecutorProvider
import com.avito.emcee.worker.internal.TestJobProducer
import com.avito.emcee.worker.internal.TestJobProducerImpl
import com.avito.emcee.worker.internal.artifacts.FileDownloader
import com.avito.emcee.worker.internal.artifacts.FileDownloaderApi.Companion.createFileDownloaderApi
import com.avito.emcee.worker.internal.consumer.RealTestJobConsumer
import com.avito.emcee.worker.internal.consumer.TestJobConsumer
import com.avito.emcee.worker.internal.identifier.HostnameWorkerIdProvider
import com.avito.emcee.worker.internal.identifier.WorkerIdProvider
import com.avito.emcee.worker.internal.networking.WorkerHostAddressResolver
import com.avito.emcee.worker.internal.registerer.WorkerRegisterer
import com.avito.emcee.worker.internal.registerer.WorkerRegistererImpl
import com.avito.emcee.worker.internal.rest.HttpServer
import com.avito.emcee.worker.internal.rest.handler.HealthCheckRequestHandler
import com.avito.emcee.worker.internal.rest.handler.ProcessingBucketsRequestHandler
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage
import com.avito.emcee.worker.internal.storage.SingleElementProcessingBucketsStorage
import com.avito.http.RetryInterceptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.logging.Logger
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class WorkerDI(
    private val config: Config,
) {

    private val workerIdProvider: WorkerIdProvider = HostnameWorkerIdProvider()
    private val bucketsStorage: ProcessingBucketsStorage = SingleElementProcessingBucketsStorage()
    private val workerHostAddressResolver = WorkerHostAddressResolver()

    private val okHttpClientBuilder = OkHttpClient.Builder().apply {
        val httpLogger = Logger.getLogger("HTTP")
        addInterceptor(HttpLoggingInterceptor { message ->
            httpLogger.finer(message)
        }.apply { level = HttpLoggingInterceptor.Level.BODY })
    }

    private val api = Retrofit.Builder().createWorkerQueueApi(
        client = okHttpClientBuilder
            .addInterceptor(
                RetryInterceptor(
                    retries = config.queue.retriesCount,
                    delayMs = config.queue.retryDelayMs,
                    allowedMethods = listOf("POST"),
                )
            )
            .build(),
        baseUrl = config.queue.url
    )

    private val fileDownloaderApi = Retrofit.Builder().createFileDownloaderApi(
        client = okHttpClientBuilder.build(),
        baseUrl = "https://stub.uses-direct-urls"
    )

    fun producer(payloadSignature: PayloadSignature): TestJobProducer {
        return TestJobProducerImpl(
            api = api,
            workerId = workerIdProvider.provide(),
            payloadSignature = payloadSignature,
        )
    }

    fun consumer(): TestJobConsumer {
        val configurations = config.configurations.associateBy(
            keySelector = { avd -> AvdConfigurationProvider.ConfigurationKey(avd.sdk, avd.type) },
            valueTransform = { avd -> AvdConfig(avd.emulatorFileName, avd.sdCardFileName) }
        )
        return RealTestJobConsumer(
            deviceProvider = SingleInstanceAndroidDeviceTestExecutorProvider(
                manager = AndroidDeviceManagerFactory.create(
                    configurationProvider = AvdConfigurationProvider(configurations),
                    androidSdk = config.androidSdkPath,
                    maximumRunningDevices = 1,
                )
            ),
            fileDownloader = FileDownloader(fileDownloaderApi),
            bucketsStorage = bucketsStorage,
            api = api,
        )
    }

    fun httpServer(): HttpServer {
        return HttpServer(
            handlers = listOf(
                ProcessingBucketsRequestHandler(bucketsStorage),
                HealthCheckRequestHandler,
            ),
        )
    }

    fun workerRegisterer(): WorkerRegisterer = WorkerRegistererImpl(
        queueApi = api,
        workerId = workerIdProvider.provide(),
        workerHostAddressResolver = workerHostAddressResolver,
    )
}
