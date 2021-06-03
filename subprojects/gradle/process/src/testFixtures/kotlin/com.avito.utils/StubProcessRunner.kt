package com.avito.utils

import com.avito.android.Result
import java.io.File
import java.time.Duration

class StubProcessRunner : ProcessRunner {
    lateinit var result: Result<String>

    override fun run(command: String, timeout: Duration) = result
    override fun spawn(command: String, outputTo: File?): Process {
        TODO("Not yet implemented")
    }
}
