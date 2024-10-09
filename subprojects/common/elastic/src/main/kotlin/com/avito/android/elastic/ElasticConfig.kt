package com.avito.android.elastic

import java.io.Serializable
import java.net.URL

public sealed class ElasticConfig : Serializable {

    public data object Disabled : ElasticConfig()

    public data class Enabled(
        val endpoints: List<URL>,
        /**
         * https://www.elastic.co/blog/what-is-an-elasticsearch-index
         */
        val indexName: String,
        /**
         * e.g. emcee-worker, gradle-build, android-test-runtime
         */
        val sourceType: String,
        /**
         * e.g. buildId, workerId
         */
        val sourceId: String,
        val authApiKey: String?
    ) : ElasticConfig()
}
