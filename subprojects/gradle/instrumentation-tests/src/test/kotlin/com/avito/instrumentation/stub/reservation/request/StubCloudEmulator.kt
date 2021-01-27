package com.avito.instrumentation.stub.reservation.request

import com.avito.instrumentation.reservation.request.Device

fun Device.CloudEmulator.Companion.createStubInstance(
    name: String = "api24",
    api: Int = 24,
    model: String = "Android_SDK_built_for_x86",
    image: String = "some/image",
    gpu: Boolean = false,
    cpuCoreLimit: String? = "1.3",
    cpuCoreRequest: String? = "1",
    memoryLimit: String? = "3.5Gi",
    memoryRequest: String? = null
) = Device.CloudEmulator(
    name = name,
    api = api,
    model = model,
    image = image,
    gpu = gpu,
    cpuCoresLimit = cpuCoreLimit,
    cpuCoresRequest = cpuCoreRequest,
    memoryLimit = memoryLimit,
    memoryRequest = memoryRequest
)
