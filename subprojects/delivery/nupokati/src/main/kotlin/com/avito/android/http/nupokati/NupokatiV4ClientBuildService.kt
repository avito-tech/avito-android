@file:Suppress("UnstableApiUsage")

package com.avito.android.http.nupokati

import com.avito.android.http.nupokati.retrofit.NupokatiV4Api
import com.avito.android.http.nupokati.retrofit.RetrofitNupokatiV4Client
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Optional
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

public abstract class NupokatiV4ClientBuildService : BuildService<NupokatiV4ClientBuildService.Params> {

    public interface Params : BuildServiceParameters {
        public val baseUrl: Property<String>

        public val chunkedUploadThresholdBytes: Property<Long>

        public val connectionTimeoutSeconds: Property<Long>

        public val readTimeoutSeconds: Property<Long>

        public val writeTimeoutSeconds: Property<Long>

        public val useTls: Property<Boolean>

        @get:Optional
        public val tlsCredentialsService: Property<TlsCredentialsService>
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }

    private val retrofit: Retrofit by lazy {
        createRetrofit()
    }

    private val api: NupokatiV4Api by lazy {
        retrofit.create(NupokatiV4Api::class.java)
    }

    private val clientInstance: NupokatiV4Client by lazy {
        RetrofitNupokatiV4Client(
            api = api,
            chunkedUploadThreshold = parameters.chunkedUploadThresholdBytes.get()
        )
    }

    public fun getClient(): NupokatiV4Client {
        return clientInstance
    }

    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(
                parameters.connectionTimeoutSeconds.get(),
                TimeUnit.SECONDS
            )
            .readTimeout(
                parameters.readTimeoutSeconds.get(),
                TimeUnit.SECONDS
            )
            .writeTimeout(
                parameters.writeTimeoutSeconds.get(),
                TimeUnit.SECONDS
            )

        if (parameters.useTls.get()) {
            val tlsManager = TlsManager(
                parameters.tlsCredentialsService.get().createCredentials()
            )
            val handshakeCertificates = tlsManager.handshakeCertificates()
            builder.sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )
        }

        return builder.build()
    }

    private fun createRetrofit(): Retrofit {
        val baseUrl = parameters.baseUrl.get()
        require(baseUrl.isNotBlank()) {
            "Base URL for NupokatiV4Client must not be blank"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .validateEagerly(true)
            .build()
    }
}
