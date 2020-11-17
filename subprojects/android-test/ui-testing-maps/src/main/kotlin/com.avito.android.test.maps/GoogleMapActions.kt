package com.avito.android.test.maps

import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.avito.android.test.maps.provider.GoogleMapProvider
import com.avito.android.test.waitFor
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.hamcrest.MatcherAssert.assertThat

interface GoogleMapActions {
    fun movePinTo(position: LatLng)
}

class GoogleMapActionsImpl(
    private val timeoutMs: Long,
    private val mapProvider: GoogleMapProvider
) : GoogleMapActions {

    override fun movePinTo(position: LatLng) {
        val map = mapProvider.provide()

        try {
            UiThreadStatement.runOnUiThread {
                map.stopAnimation()
                map.animateCamera(CameraUpdateFactory.newLatLng(position))
            }

            waitFor(timeoutMs = timeoutMs) {
                UiThreadStatement.runOnUiThread {
                    assertThat(
                        map.cameraPosition.target,
                        hasTheSameCoordinatesAs(position)
                    )
                }
            }
        } catch (t: Throwable) {
            throw GoogleMapElementException(
                "Something went wrong during moving camera to position: $position",
                t
            )
        }
    }
}
