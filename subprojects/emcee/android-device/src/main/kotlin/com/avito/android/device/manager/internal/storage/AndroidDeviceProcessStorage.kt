package com.avito.android.device.manager.internal.storage

import java.util.Collections

internal class AndroidDeviceProcessStorage {

    private val map: MutableMap<AndroidDeviceCoordinates, AndroidDeviceProcess> =
        Collections.synchronizedMap(mutableMapOf())

    fun put(coordinates: AndroidDeviceCoordinates, process: AndroidDeviceProcess) {
        map[coordinates] = process
    }

    fun pop(coordinates: AndroidDeviceCoordinates): AndroidDeviceProcess {
        return requireNotNull(map.remove(coordinates)) {
            "There is not AndroidDeviceProcess associated with $coordinates"
        }
    }
}
