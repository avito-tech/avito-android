package com.avito.runner.config

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class TestsCountBasedReservationTest {
    /**
     * Setup is expected to have testsCount * guaranteedTries = 12
     */
    private val reservation = Reservation.TestsCountBasedReservation(
        device = com.avito.instrumentation.reservation.request.Device.MockEmulator("stub", "stub", 0),
        quota = QuotaConfigurationData(3, 1, 0),
        testsPerEmulator = 12,
        maximum = 10,
        minimum = 1
    )

    @Test
    fun `when tests count and tests per emulator have division with remainder`() {
        Truth.assertThat(reservation.data(13).count).isEqualTo(2)
        Truth.assertThat(reservation.data(11).count).isEqualTo(1)
    }

    @Test
    fun `when tests count and tests per emulator have division without remainder`() {
        Truth.assertThat(reservation.data(24).count).isEqualTo(2)
    }
}
