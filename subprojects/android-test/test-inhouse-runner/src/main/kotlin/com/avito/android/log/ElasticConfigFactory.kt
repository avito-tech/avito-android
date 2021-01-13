package com.avito.android.log

import com.avito.android.elastic.ElasticConfig
import com.avito.android.test.report.ArgsProvider
import java.net.URL

// todo should be in :android-log,
//  but ArgsProvider is in :test-report, should be on lower level module, so android-log could depend on it
internal object ElasticConfigFactory {

    fun parse(argsProvider: ArgsProvider): ElasticConfig {
        val isElasticEnabled = argsProvider.getOptionalArgument("elasticEnabled") == "true"

        return if (isElasticEnabled) {
            val endpointsRawValue: String = argsProvider.getMandatoryArgument("elasticEndpoints")
            val indexPattern: String = argsProvider.getMandatoryArgument("elasticIndexPattern")
            val buildId = argsProvider.getMandatoryArgument("buildId")

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
        rawEndpointsValue.split(',').map { URL(it) }
}
