package com.avito.android.graphite

import com.avito.logger.LoggerFactory

public interface GraphiteSender {

    /**
     * Incubating
     */
    public fun send(metric: GraphiteMetric)

    public companion object {

        public fun create(
            config: GraphiteConfig,
            loggerFactory: LoggerFactory,
            isTest: Boolean,
        ): GraphiteSender {
            val transport = if (isTest) {
                GraphiteTransport.Test()
            } else {
                GraphiteTransport.Real(config.host, config.port)
            }
            return GraphiteSenderImpl(config, transport, loggerFactory)
        }
    }
}
