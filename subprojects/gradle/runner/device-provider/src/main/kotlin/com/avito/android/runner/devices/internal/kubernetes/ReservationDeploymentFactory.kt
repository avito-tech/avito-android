package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData
import io.fabric8.kubernetes.api.model.apps.Deployment

internal interface ReservationDeploymentFactory {
    fun createDeployment(namespace: String, reservation: ReservationData): Deployment
}
