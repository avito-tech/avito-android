package com.avito.utils

import org.funktionale.tries.Try

class StubProcessRunner : ProcessRunner {

    lateinit var result: Try<String>

    override fun run(command: String): Try<String> = result
}
