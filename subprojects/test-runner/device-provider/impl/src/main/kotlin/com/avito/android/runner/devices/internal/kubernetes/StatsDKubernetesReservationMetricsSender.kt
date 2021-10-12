package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric

/**
 * Send the queue time for each requested POD
 */
internal class StatsDKubernetesReservationMetricsSender(
    private val statsDSender: StatsDSender,
    private val state: KubernetesReservationState,
    runnerPrefix: SeriesName
) : KubernetesReservationListener {

    private val queueSeriesName = runnerPrefix.append("reservation.pod.queue", multipart = true)

    override suspend fun onClaim(reservations: Collection<ReservationData>) {
        val requestedPodCount = reservations.fold(0) { acc, reservation -> acc + reservation.count }
        state.claim(requestedPodCount)
    }

    override suspend fun onPodAcquired() {
        val queueTime = state.podAcquired()
        sendQueueTime(queueTime)
    }

    override suspend fun onPodRemoved() {
        state.podRemoved()
    }

    override suspend fun onRelease() {
        state.release().forEach { queueTime ->
            sendQueueTime(queueTime)
        }
    }

    private fun sendQueueTime(queueTime: KubernetesReservationState.QueueTime) {
        statsDSender.send(StatsMetric.time(queueSeriesName, queueTime.value.seconds))
    }
}
