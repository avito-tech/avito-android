package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.PodRequest
import com.avito.time.TimeProvider
import java.time.Duration
import java.time.Instant
import java.util.concurrent.PriorityBlockingQueue

/**
 * Hold data about POD requests
 *
 * When new POD acquired calculate queue time for that POD
 *
 * Assumption, when we [podRemoved] Kubernetes will create a replacement which will be acquired in the future
 *
 * The queue time for PODs those weren't acquired is [release] time minus [PodRequest.requestTime]
 */
internal class KubernetesReservationState(
    private val timeProvider: TimeProvider
) {

    private val requests = PriorityBlockingQueue<PodRequest>()

    fun claim(requestedPodCount: Int) {
        val claimTime = timeProvider.nowInstant()
        requests.addAll(
            (0 until requestedPodCount).map { PodRequest(claimTime) }
        )
    }

    fun podAcquired(): QueueTime {
        val request = checkNotNull(requests.poll()) {
            "Pod requests queue is empty"
        }
        val queueTime = Duration.between(request.requestTime, timeProvider.nowInstant())
        return QueueTime(queueTime)
    }

    fun podRemoved() {
        requests.add(
            PodRequest(timeProvider.nowInstant())
        )
    }

    fun release(): List<QueueTime> {
        val releaseTime = timeProvider.nowInstant()
        return requests.toList().map { request ->
            QueueTime(
                Duration.between(request.requestTime, releaseTime)
            )
        }
    }

    /**
     * Duration between time when POD was requested and time when POD was acquired
     */
    data class QueueTime(val value: Duration)

    private class PodRequest(val requestTime: Instant) : Comparable<PodRequest> {
        override fun compareTo(other: PodRequest): Int {
            return requestTime.compareTo(other.requestTime)
        }
    }
}
