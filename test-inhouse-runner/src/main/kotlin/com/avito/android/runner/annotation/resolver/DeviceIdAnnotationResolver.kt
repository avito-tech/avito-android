package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.DeviceId

class DeviceIdAnnotationResolver : AnnotationResolver<DeviceId>(
    "deviceId",
    DeviceId::class.java,
    { annotation -> TestMetadataResolver.Resolution.ReplaceString(annotation.deviceId) }
)
