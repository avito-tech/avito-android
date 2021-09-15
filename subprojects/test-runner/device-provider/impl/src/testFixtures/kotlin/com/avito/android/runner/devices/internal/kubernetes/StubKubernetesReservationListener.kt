package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData

internal object StubKubernetesReservationListener : KubernetesReservationListener {

    override fun onClaim(reservations: Collection<ReservationData>) {
        // empty
    }

    override fun onPodAcquired() {
        // empty
    }

    override fun onPodRemoved() {
        // empty
    }

    override fun onRelease() {
        // empty
    }
}
