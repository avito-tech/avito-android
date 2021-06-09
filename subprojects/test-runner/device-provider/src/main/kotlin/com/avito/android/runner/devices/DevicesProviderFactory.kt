package com.avito.android.runner.devices

import java.io.File

public interface DevicesProviderFactory {
    public fun create(tempLogcatDir: File): DevicesProvider
}
