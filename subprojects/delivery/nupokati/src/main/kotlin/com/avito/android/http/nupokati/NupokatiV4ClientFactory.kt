package com.avito.android.http.nupokati

import com.avito.android.http.nupokati.retrofit.NupokatiV4Api
import com.avito.android.http.nupokati.retrofit.RetrofitNupokatiV4Client
import com.avito.android.stats.StatsDSender
import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.manager.TlsManager
import com.avito.http.RetryInterceptor
import com.avito.http.StatsDHttpEventListener
import com.avito.http.TagRequestMetadataProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

internal object NupokatiV4ClientFactory {

    fun create(
        baseUrl: String,
        connectionTimeoutSeconds: Long,
        readTimeoutSeconds: Long,
        writeTimeoutSeconds: Long,
        chunkedUploadThresholdBytes: Long,
        tlsCredentials: TlsCredentials?,
        statsDSender: StatsDSender,
        timeProvider: TimeProvider,
        loggerFactory: LoggerFactory,
    ): NupokatiV4Client {
        require(baseUrl.isNotBlank()) {
            "Base URL for NupokatiV4Client must not be blank"
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(
                RetryInterceptor(
                    retries = 3,
                    allowedMethods = listOf("POST"),
                )
            )
            .eventListenerFactory {
                StatsDHttpEventListener(
                    statsDSender = statsDSender,
                    timeProvider = timeProvider,
                    requestMetadataProvider = TagRequestMetadataProvider(),
                    loggerFactory = loggerFactory,
                )
            }

        if (tlsCredentials != null) {
            val tlsManager = TlsManager(tlsCredentials)
            val handshakeCertificates = tlsManager.handshakeCertificates()
            builder.sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .validateEagerly(true)
            .build()

        return RetrofitNupokatiV4Client(
            api = retrofit.create(NupokatiV4Api::class.java),
            chunkedUploadThreshold = chunkedUploadThresholdBytes,
        )
    }
}
