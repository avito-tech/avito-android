package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.model.ReservationData
import com.fkorotkov.kubernetes.apps.metadata
import io.fabric8.kubernetes.api.model.apps.Deployment

internal class FakeReservationDeploymentFactory: ReservationDeploymentFactory {

    override fun createDeployment(namespace: String, reservation: ReservationData): Deployment {
        return Deployment().apply {
            metadata {
                name = "stub-deployment-name"
            }
        }
    }
}
