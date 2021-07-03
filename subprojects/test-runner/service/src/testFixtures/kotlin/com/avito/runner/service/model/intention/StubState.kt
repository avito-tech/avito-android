package com.avito.runner.service.model.intention

internal fun State.Layer.InstalledApplication.Companion.createStubInstance(
    applicationPackage: String = "com.test",
    applicationPath: String = "/app"
): State.Layer.InstalledApplication = State.Layer.InstalledApplication(
    applicationPackage = applicationPackage,
    applicationPath = applicationPath
)
