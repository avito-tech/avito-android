package com.avito.android.runner

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

internal fun Context.checkPlayServices() {
    when (val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
        ConnectionResult.SUCCESS -> {
        }
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> error("Play services requires update")
        else -> error("Play Services unavailable: com.google.android.gms.common.ConnectionResult = $result")
    }
}
