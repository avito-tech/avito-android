package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import com.avito.logger.LoggerFactory
import com.avito.logger.create

/**
 * Send the queue time for each requested POD
 */
internal class StatsDKubernetesReservationMetricsSender(
    private val statsDSender: StatsDSender,
    private val state: KubernetesReservationState,
    runnerPrefix: SeriesName,
    loggerFactory: LoggerFactory
) : KubernetesReservationListener {

    private val loggerFactory = loggerFactory.create<KubernetesReservationListener>()
    private val queueSeriesName = runnerPrefix.append("reservation.pod.queue", multipart = true)

    override suspend fun onClaim(reservations: Collection<ReservationData>) {
        loggerFactory.info("onClaim $reservations")
        val requestedPodCount = reservations.fold(0) { acc, reservation -> acc + reservation.count }
        state.claim(requestedPodCount)
    }

    override suspend fun onPodAcquired() {
        loggerFactory.info("onPodAcquired")
        val queueTime = state.podAcquired()
        sendQueueTime(queueTime)
    }

    override suspend fun onPodRemoved() {
        loggerFactory.info("onPodRemoved")
        state.podRemoved()
    }

    override suspend fun onRelease() {
        loggerFactory.info("onRelease")
        state.release().forEach { queueTime ->
            sendQueueTime(queueTime)
        }
    }

    private fun sendQueueTime(queueTime: KubernetesReservationState.QueueTime) {
        statsDSender.send(StatsMetric.time(queueSeriesName, queueTime.value.seconds))
    }
}
