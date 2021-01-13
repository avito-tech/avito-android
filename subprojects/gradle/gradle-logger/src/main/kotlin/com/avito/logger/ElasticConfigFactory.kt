package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project
import java.net.URL

internal object ElasticConfigFactory {

    fun config(project: Project): ElasticConfig {
        val isElasticEnabled = project.properties["avito.elastic.enabled"].toString() == "true"

        return if (isElasticEnabled) {
            val endpointsRawValue: String? = project.properties["avito.elastic.endpoints"]?.toString()
            require(!endpointsRawValue.isNullOrBlank()) { "avito.elastic.endpoints has not been provided" }

            val indexPattern: String? = project.properties["avito.elastic.indexpattern"]?.toString()
            require(!indexPattern.isNullOrBlank()) { "avito.elastic.indexpattern has not been provided" }

            val buildId = project.envArgs.build.id.toString()

            ElasticConfig.Enabled(
                endpoints = parseEndpoint(endpointsRawValue),
                indexPattern = indexPattern,
                buildId = buildId
            )
        } else {
            ElasticConfig.Disabled
        }
    }

    private fun parseEndpoint(rawEndpointsValue: String): List<URL> =
        rawEndpointsValue.split('|').map { URL(it) }
}
