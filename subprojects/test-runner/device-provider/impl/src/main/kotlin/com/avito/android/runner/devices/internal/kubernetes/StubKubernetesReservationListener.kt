package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData

internal object StubKubernetesReservationListener : KubernetesReservationListener {

    override suspend fun onClaim(reservations: Collection<ReservationData>) {
        // empty
    }

    override suspend fun onPodAcquired() {
        // empty
    }

    override suspend fun onPodRemoved() {
        // empty
    }

    override suspend fun onRelease() {
        // empty
    }
}
