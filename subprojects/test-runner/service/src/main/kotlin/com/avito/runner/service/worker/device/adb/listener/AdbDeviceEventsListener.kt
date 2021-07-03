package com.avito.runner.service.worker.device.adb.listener

public interface AdbDeviceEventsListener :
    AdbDeviceGetSdkListener,
    AdbDeviceInstallApplicationListener,
    AdbDeviceGetAliveListener,
    AdbDeviceClearPackageListener,
    AdbDevicePullListener,
    AdbDeviceClearDirectoryListener,
    AdbDeviceListListener,
    AdbDeviceGetLogcatListener,
    AdbDeviceRunTestListener
