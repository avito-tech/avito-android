package com.avito.android.graphite

import com.avito.logger.LoggerFactory

public interface GraphiteSender {

    /**
     * Incubating
     */
    public fun send(metric: GraphiteMetric)

    public companion object {

        public fun create(config: GraphiteConfig, loggerFactory: LoggerFactory): GraphiteSender {
            return GraphiteSenderImpl(config, loggerFactory)
        }
    }
}
