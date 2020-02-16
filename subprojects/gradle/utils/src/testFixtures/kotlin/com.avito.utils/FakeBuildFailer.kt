package com.avito.utils

class FakeBuildFailer : BuildFailer {

    var lastReason: String? = null
    var lastCause: Throwable? = null

    override fun failBuild(message: String, cause: Throwable) {
        lastReason = message
    }
}
