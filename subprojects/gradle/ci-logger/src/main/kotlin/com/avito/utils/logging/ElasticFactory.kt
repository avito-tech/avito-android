package com.avito.utils.logging

import com.avito.android.elastic.ElasticClient
import com.avito.android.elastic.HttpElasticClient
import com.avito.android.elastic.StubElasticClient
import com.avito.time.DefaultTimeProvider
import com.avito.utils.gradle.envArgs
import okhttp3.OkHttpClient
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

internal object ElasticFactory {

    private const val defaultTimeoutSec = 10L

    fun create(project: Project): ElasticClient {
        val isElasticEnabled = project.properties["avito.elastic.enabled"].toString() == "true"

        return if (isElasticEnabled) {
            val endpoint: String? = project.properties["avito.elastic.endpoint"]?.toString()
            require(!endpoint.isNullOrBlank()) { "avito.elastic.endpoints has not been provided" }

            val indexPattern: String? = project.properties["avito.elastic.indexpattern"]?.toString()
            require(!indexPattern.isNullOrBlank()) { "avito.elastic.indexpattern has not been provided" }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                .build()

            HttpElasticClient(
                okHttpClient = okHttpClient,
                timeProvider = DefaultTimeProvider(),
                endpoint = endpoint,
                indexPattern = indexPattern,
                buildId = project.envArgs.build.id.toString(),
                onError = { msg, e -> project.logger.error(msg, e) }
            )
        } else {
            StubElasticClient
        }
    }
}
