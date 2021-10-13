package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData

internal interface KubernetesReservationListener {
    suspend fun onClaim(reservations: Collection<ReservationData>)
    suspend fun onPodAcquired()
    suspend fun onPodRemoved()
    suspend fun onRelease()
}
