package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData

internal interface KubernetesReservationListener {
    fun onClaim(reservations: Collection<ReservationData>)
    fun onPodAcquired()
    fun onPodRemoved()
    fun onRelease()
}
