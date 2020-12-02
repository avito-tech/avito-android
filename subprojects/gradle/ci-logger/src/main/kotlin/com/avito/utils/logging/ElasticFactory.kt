package com.avito.utils.logging

import com.avito.android.elastic.Elastic
import com.avito.android.elastic.MultipleEndpointsElastic
import com.avito.android.elastic.StubElastic
import com.avito.time.DefaultTimeProvider
import com.avito.utils.gradle.envArgs
import okhttp3.OkHttpClient
import org.gradle.api.Project

internal object ElasticFactory {

    fun create(project: Project): Elastic {
        
        val isElasticEnabled = project.properties["avito.elastic.enabled"].toString() == "true"

        return if (isElasticEnabled) {
            val endpoints: List<String> =
                requireNotNull(
                    project.properties["avito.elastic.endpoints"]
                        ?.toString()
                        ?.split("|")
                ) {
                    "avito.elastic.endpoints has not been provided"
                }

            val indexPattern: String = requireNotNull(project.properties["avito.elastic.indexpattern"]?.toString()) {
                "avito.elastic.indexpattern has not been provided"
            }

            MultipleEndpointsElastic(
                okHttpClient = OkHttpClient(),
                timeProvider = DefaultTimeProvider(),
                endpoints = endpoints,
                indexPattern = indexPattern,
                buildId = project.envArgs.build.id.toString(),
                onError = { msg, e -> project.logger.error(msg, e) }
            )
        } else {
            StubElastic
        }
    }
}
