package com.avito.android.runner

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

internal fun Context.checkPlayServices() {
    when (val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
        ConnectionResult.SUCCESS -> {
        }
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> error(
            "Play services requires update. " +
                "Required: $playServicesMetaDataVersion, " +
                "on device: $playServicesOnDeviceVersion"
        )
        else -> error("Play Services unavailable: com.google.android.gms.common.ConnectionResult = $result")
    }
}

@Suppress("DEPRECATION")
internal val Context.playServicesOnDeviceVersion: Int
    get() = packageManager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode

internal val Context.playServicesMetaDataVersion: Int
    get() =
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData.getInt("${GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE}.version")
