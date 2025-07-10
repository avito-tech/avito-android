package com.avito.runner.service.worker.device.adb

// TODO duplicate part of [com.avito.android.AndroidSdk.androidHome]
public class Adb {

    public val adbPath: String

    init {
        val androidHome: String = requireNotNull(System.getenv("ANDROID_HOME")) {
            "Can't find env ANDROID_HOME. It needs to run 'adb'"
        }
        adbPath = "$androidHome/platform-tools/adb"
    }

    override fun toString(): String {
        return adbPath
    }
}
