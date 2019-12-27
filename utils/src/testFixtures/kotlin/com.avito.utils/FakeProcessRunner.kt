package com.avito.utils

import org.funktionale.tries.Try

class FakeProcessRunner : ProcessRunner {

    lateinit var result: Try<String>

    override fun run(command: String): Try<String> = result
}
