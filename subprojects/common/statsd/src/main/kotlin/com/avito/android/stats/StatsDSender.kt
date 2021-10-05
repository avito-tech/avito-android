package com.avito.android.stats

import com.avito.logger.LoggerFactory

public interface StatsDSender {

    public fun send(metric: StatsMetric)

    public companion object {

        public fun create(
            config: StatsDConfig,
            loggerFactory: LoggerFactory
        ): StatsDSender {
            return StatsDSenderImpl(config, loggerFactory)
        }
    }
}
