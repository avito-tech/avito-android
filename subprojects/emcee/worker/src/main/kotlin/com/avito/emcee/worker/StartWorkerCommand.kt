package com.avito.emcee.worker

import com.avito.android.device.avd.AvdConfig
import com.avito.android.device.avd.internal.AvdConfigurationProvider
import com.avito.android.device.manager.AndroidDeviceManager
import com.avito.emcee.worker.WorkerQueueApi.Companion.createWorkerQueueApi
import com.avito.emcee.worker.internal.SingleInstanceAndroidDeviceTestExecutorProvider
import com.avito.emcee.worker.internal.TestJobConsumerImpl
import com.avito.emcee.worker.internal.TestJobProducerImpl
import com.avito.emcee.worker.internal.artifacts.FileDownloader
import com.avito.emcee.worker.internal.artifacts.FileDownloaderApi.Companion.createFileDownloaderApi
import com.avito.emcee.worker.internal.networking.SocketAddressResolver
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.nio.file.Path
import kotlin.time.ExperimentalTime

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@ExperimentalTime
@ExperimentalCli
internal class StartWorkerCommand(
    name: String,
    description: String
) : Subcommand(name, description) {

    private val configPath: String by option(
        type = ArgType.String,
        fullName = "config",
        shortName = "c",
        description = "Absolute path to worker config"
    ).required()

    private val debugMode: Boolean by option(
        type = ArgType.Boolean,
        fullName = "debug",
        shortName = "d",
        description = "Enables verbose logging",
    ).default(false)

    override fun execute() {
        val moshi = Moshi.Builder().build()
        val okHttpClient = OkHttpClient.Builder().apply {
            if (debugMode) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }.build()
        val socketAddressResolver = SocketAddressResolver()
        val configAdapter = moshi.adapter<Config>()
        val config: Config = requireNotNull(
            configAdapter.fromJson(File(configPath).readText())
        )
        val producer = TestJobProducerImpl(
            api = Retrofit.Builder().createWorkerQueueApi(okHttpClient, config.queueUrl),
            workerId = config.workerId,
            workerAddress = socketAddressResolver.resolve(config.workerPort)
        )
        val fileDownloader = FileDownloader(
            api = Retrofit.Builder().createFileDownloaderApi(okHttpClient, config.queueUrl)
        )
        val consumer = TestJobConsumerImpl(
            deviceProvider = SingleInstanceAndroidDeviceTestExecutorProvider(
                AndroidDeviceManager.create(
                    configurationProvider = AvdConfigurationProvider(
                        config.avd.associateBy(
                            keySelector = { AvdConfigurationProvider.ConfigurationKey(it.sdk, it.type) },
                            valueTransform = { AvdConfig(it.emulatorFileName, it.sdCardFileName) }
                        )
                    ),
                    androidSdk = Path.of(config.androidSdkPath)
                )
            ),
            fileDownloader = fileDownloader
        )
        runBlocking {
            consumer.consume(
                producer.getJobs()
            ).collect { result ->
                println(result)
            }
        }
    }
}
