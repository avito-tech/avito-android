package com.avito.test.summary

import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender
import com.avito.report.model.CrossDeviceSuite
import org.funktionale.tries.Try

internal class GraphiteRunWriter(private val statsSender: StatsDSender) {

    private val prefix = "avito.test.instrumentation.run"

    fun write(testData: CrossDeviceSuite): Try<Unit> = Try {
        statsSender.send(prefix, GaugeMetric("success", testData.success))
        statsSender.send(
            prefix,
            GaugeMetric("failed", testData.failedOnAllDevicesCount + testData.failedOnSomeDevicesCount)
        )
        statsSender.send(prefix, GaugeMetric("manual", testData.manualCount))
        statsSender.send(prefix, GaugeMetric("automated", testData.automatedCount))

        statsSender.send(prefix, GaugeMetric("inconsistent", testData.inconsistentCount))

        //todo довольно странная метрика в измерении всех девайсов, считали неверно
        //statsSender.send(prefix, GaugeMetric("flaky", testData.success))
        //statsSender.send(prefix, GaugeMetric("stable", testData.success))

        //todo научится отправлять сначала в репорт
        //statsSender.send(prefix, GaugeMetric("lost", testData.inconsistentCount))
    }
}
