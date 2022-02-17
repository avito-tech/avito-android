package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.PodRequest
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.State.CLAIMED
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.State.INITIAL
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.State.RELEASED
import com.avito.time.TimeProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.PriorityBlockingQueue

/**
 * Holds data about POD requests
 *
 * When new POD acquired, calculate queue time for that POD
 *
 * Assumption: when we [podRemoved] Kubernetes will create a replacement which will be acquired in the future
 *
 * The queue time for PODs those weren't acquired is [release] time minus [PodRequest.requestTime]
 */
internal class KubernetesReservationState(
    private val timeProvider: TimeProvider
) {

    private val requests = PriorityBlockingQueue<PodRequest>()
    private var state = INITIAL
    private val lock = Mutex()

    suspend fun claim(requestedPodCount: Int): Unit = lock.withLock {
        checkState(INITIAL)
        val claimTime = timeProvider.nowInstant()
        requests.addAll(
            (0 until requestedPodCount).map { PodRequest(claimTime) }
        )
        state = CLAIMED
    }

    suspend fun podAcquired(): QueueTime = lock.withLock {
        checkState(CLAIMED)
        val request = checkNotNull(requests.poll()) {
            "Error while calculating acquired pods metrics: requests queue is empty"
        }
        val queueTime = Duration.between(request.requestTime, timeProvider.nowInstant())
        QueueTime(queueTime)
    }

    suspend fun podRemoved(): Unit = lock.withLock {
        checkState(CLAIMED)
        requests.add(
            PodRequest(timeProvider.nowInstant())
        )
    }

    suspend fun release(): List<QueueTime> = lock.withLock {
        checkState(CLAIMED)
        val releaseTime = timeProvider.nowInstant()
        val result = requests.toList().map { request ->
            QueueTime(
                Duration.between(request.requestTime, releaseTime)
            )
        }
        state = RELEASED
        result
    }

    private fun checkState(expected: State) {
        check(state == expected) {
            "Must be $expected but was $state"
        }
    }

    /**
     * Duration between time when POD was requested and time when POD was acquired
     */
    data class QueueTime(val value: Duration)

    private enum class State {
        INITIAL,
        CLAIMED,
        RELEASED
    }

    private class PodRequest(val requestTime: Instant) : Comparable<PodRequest> {
        override fun compareTo(other: PodRequest): Int {
            return requestTime.compareTo(other.requestTime)
        }
    }
}
