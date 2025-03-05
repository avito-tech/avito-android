package com.avito.android.clickstream

import com.avito.android.clickstream.api.ClickStreamApi
import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.clickstream.config.ClickStreamConfig
import com.avito.android.clickstream.config.clickStreamConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

public abstract class ClickStreamEventService : BuildService<ClickStreamEventService.Params> {

    public interface Params : BuildServiceParameters {
        public val clickStreamConfig: Property<ClickStreamConfig>
    }

    private val clickStreamApi: ClickStreamApi by lazy {
        Retrofit.Builder()
            .baseUrl(parameters.clickStreamConfig.get().serviceUrl)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(buildClient())
            .build()
            .create(ClickStreamApi::class.java)
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    public fun sendEvents(envelope: ClickStreamEventRequest) {
        try {
            val response = clickStreamApi.sendEvents(envelope).execute()
            if (!response.isSuccessful) throw HttpException(response)
        } catch (error: Throwable) {
            throw IllegalStateException("Failed to send ClickStream events", error)
        }
    }

    public companion object {
        public fun provideClickStreamEventService(
            project: Project,
        ): Provider<ClickStreamEventService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<ClickStreamEventService> {
            return project.gradle.sharedServices.registerIfAbsent(
                ClickStreamEventService::class.java.name,
                ClickStreamEventService::class.java,
            ) {
                it.parameters { params ->
                    params.clickStreamConfig.set(project.clickStreamConfig)
                }
            }
        }
    }
}
