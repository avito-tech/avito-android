package com.avito.android.test.maps

import androidx.annotation.IdRes
import com.avito.android.test.maps.provider.FragmentGoogleMapProvider
import com.avito.android.test.maps.provider.GoogleMapProvider
import com.avito.android.test.page_object.PageObject
import java.util.concurrent.TimeUnit

class GoogleMapFragmentElement(
    @IdRes private val id: Int,
    private val mapProvider: GoogleMapProvider = FragmentGoogleMapProvider(
        id = id,
        timeoutMs = checksTimeoutMs
    )
) : PageObject(), GoogleMapActions by GoogleMapActionsImpl(
    timeoutMs = checksTimeoutMs,
    mapProvider = mapProvider
) {

    val checks: GoogleMapChecks =
        GoogleMapChecksImpl(timeoutMs = checksTimeoutMs, mapProvider = mapProvider)
}

private val checksTimeoutMs = TimeUnit.SECONDS.toMillis(10)
