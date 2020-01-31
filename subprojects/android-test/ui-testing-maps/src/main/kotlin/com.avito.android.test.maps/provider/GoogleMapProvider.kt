package com.avito.android.test.maps.provider

import com.google.android.gms.maps.GoogleMap

interface GoogleMapProvider {
    fun provide(): GoogleMap
}
