package com.avito.utils

import com.avito.android.Result

class StubProcessRunner : ProcessRunner {

    lateinit var result: Result<String>

    override fun run(command: String): Result<String> = result
}
