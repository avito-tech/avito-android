package com.avito.utils

import com.avito.utils.BuildFailer

class FakeBuildFailer : BuildFailer {

    var lastReason: String? = null

    override fun failBuild(reason: String) {
        lastReason = reason
    }
}
