package com.avito.android.network_contracts.shared

import com.avito.android.Result
import com.avito.utils.ProcessRunner
import java.time.Duration

internal fun ProcessRunner.runCommand(command: String): Result<String> {
    return run(command, Duration.ofSeconds(5))
}
