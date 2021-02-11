package com.avito.android.graphite

class StubGraphiteSender : GraphiteSender {

    val metrics = mutableListOf<GraphiteMetric>()

    override fun send(metric: GraphiteMetric) {
        metrics.add(metric)
    }
}
