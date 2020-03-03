package com.avito.android.graphite

import com.avito.android.graphite.GraphiteSender

class FakeGraphiteSender: GraphiteSender {

    val metrics = mutableListOf<GraphiteMetric>()

    override fun send(metric: GraphiteMetric) {
        metrics.add(metric)
    }
}
