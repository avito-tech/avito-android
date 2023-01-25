package com.avito.emcee.discoverer.tests

import com.linkedin.dex.parser.DexParser
import java.nio.file.Path

internal class TestDiscoverer {

    fun discover(path: Path): TestDiscoveryResults {
        return TestDiscoveryResults(DexParser.findTestNames(path.toString()))
    }
}
