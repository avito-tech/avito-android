package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationState.QueueTime
import com.avito.time.TimeMachineProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

internal class KubernetesReservationStateTest {

    private val timeProvider = TimeMachineProvider()
    private val state = KubernetesReservationState(timeProvider)

    @Test
    fun `pod acquired before claim - IllegalStateException`() {
        val exception = assertThrows<IllegalStateException> {
            state.podAcquired()
        }

        assertThat(exception.message).isEqualTo("Pod requests queue is empty")
    }

    @Test
    fun `pod acquired - queue time is equal to acquired time minus claim time`() {
        state.claim(requestedPodCount = 1)

        val actualPodAcquiredDuration = Duration.ofSeconds(1)
        timeProvider.moveForwardOn(actualPodAcquiredDuration)
        val queueTime = state.podAcquired()

        assertThat(queueTime)
            .isEqualTo(QueueTime(actualPodAcquiredDuration))
    }

    @Test
    fun `release - queue times are equal the amount of pod requests`() {
        state.claim(requestedPodCount = 2)

        val queueTimes = state.release()

        assertThat(
            queueTimes
        ).hasSize(2)
    }

    @Test
    fun `release - queue time is equal to release time minus request time`() {
        state.claim(requestedPodCount = 1)

        val releaseTimeDuration = Duration.ofSeconds(1)
        timeProvider.moveForwardOn(releaseTimeDuration)
        val queueTimes = state.release()

        assertThat(queueTimes).hasSize(1)
        val actualQueueTime = queueTimes[0]
        assertThat(actualQueueTime)
            .isEqualTo(QueueTime(releaseTimeDuration))
    }

    @Test
    fun `pod removed then release - queue time is equal to release time minus pod removed time`() {
        state.claim(0)
        state.podRemoved()

        val releaseTimeDuration = Duration.ofSeconds(1)
        timeProvider.moveForwardOn(releaseTimeDuration)
        val queueTimes = state.release()

        assertThat(queueTimes).hasSize(1)
        val actualQueueTime = queueTimes[0]
        assertThat(actualQueueTime)
            .isEqualTo(QueueTime(releaseTimeDuration))
    }
}
