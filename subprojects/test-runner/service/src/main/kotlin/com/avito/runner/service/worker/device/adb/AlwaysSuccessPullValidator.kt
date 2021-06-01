package com.avito.runner.service.worker.device.adb

import java.nio.file.Path

internal object AlwaysSuccessPullValidator : PullValidator {

    override fun isPulledCompletely(hostDir: Path): PullValidator.Result = PullValidator.Result.Ok
}
