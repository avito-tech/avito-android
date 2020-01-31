package com.avito.android.test.maps

import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.avito.android.test.maps.provider.GoogleMapProvider
import com.avito.android.test.waitFor
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert

interface GoogleMapChecks {
    fun pinAtPosition(position: LatLng)
}

class GoogleMapChecksImpl(
    private val timeoutMs: Long,
    private val mapProvider: GoogleMapProvider
) : GoogleMapChecks {
    override fun pinAtPosition(position: LatLng) {
        val map = mapProvider.provide()

        waitFor(timeoutMs = timeoutMs) {
            UiThreadStatement.runOnUiThread {
                Assert.assertThat(
                    map.cameraPosition.target,
                    hasTheSameCoordinatesAs(position)
                )
            }
        }
    }
}
