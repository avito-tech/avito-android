package com.avito.runner.service.worker.device.adb

class Adb {
    val adb: String

    init {
        val androidHome: String = requireNotNull(System.getenv("ANDROID_HOME")) {
            "Can't find env ANDROID_HOME. It needs to run 'adb'"
        }
        adb = "$androidHome/platform-tools/adb"
    }

    override fun toString(): String {
        return adb
    }
}
