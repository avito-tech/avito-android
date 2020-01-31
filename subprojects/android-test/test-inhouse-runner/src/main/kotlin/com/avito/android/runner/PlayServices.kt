package com.avito.android.runner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

@SuppressLint("LogNotTimber")
internal fun Context.checkPlayServices() {
    Log.d(
        "LaunchRule",
        "Required play services version: $playServicesMetaDataVersion, on device: $playServicesOnDeviceVersion"
    )
    when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
        ConnectionResult.SUCCESS -> {
        }
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> error("Play services requires update")
        else -> error("Play Services unavailable")
    }
}

internal val Context.playServicesOnDeviceVersion: Int
    get() = packageManager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode

internal val Context.playServicesMetaDataVersion: Int
    get() =
        packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData.getInt("${GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE}.version")
