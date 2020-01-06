package com.avito.utils

class FakeBuildFailer : BuildFailer {

    var lastReason: String? = null

    override fun failBuild(reason: String) {
        lastReason = reason
    }
}
