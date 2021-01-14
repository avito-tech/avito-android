package com.avito.android.log

import com.avito.android.elastic.ElasticConfig
import com.avito.logger.LoggingDestination
import com.avito.logger.destination.ElasticDestination

internal object ElasticDestinationFactory {

    fun create(config: ElasticConfig, metadata: AndroidMetadata): LoggingDestination {
        return ElasticDestination(
            config = config,
            metadata = metadata.toMap()
        )
    }
}
