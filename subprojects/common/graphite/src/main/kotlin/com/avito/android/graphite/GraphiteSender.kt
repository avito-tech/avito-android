package com.avito.android.graphite

public interface GraphiteSender {

    /**
     * Incubating
     */
    public fun send(metric: GraphiteMetric)

    public companion object {

        public fun create(config: GraphiteConfig): GraphiteSender {
            return GraphiteSenderImpl(config)
        }
    }
}
