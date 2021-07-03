package com.avito.utils

import com.avito.android.Result
import java.io.File
import java.time.Duration

public interface ProcessRunner {

    /**
     * @param command i.e. ls -la
     *
     * @return output команды, если exit code = 0
     */
    public fun run(command: String, timeout: Duration): Result<String>

    public fun spawn(command: String, outputTo: File?): Process

    public companion object {

        public fun create(workingDirectory: File?): ProcessRunner = RealProcessRunner(workingDirectory)
    }
}
