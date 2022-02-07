package com.avito.emcee.internal

import com.avito.emcee.EmceeTestActionConfig
import okio.buffer
import okio.sink
import okio.source
import java.io.File

internal class EmceeConfigTestHelper(outputDir: File) {

    private val configAdapter = EmceeTestActionConfig.createMoshiAdapter()

    private val configDumpFile = File(outputDir, "config-dump.json").also { it.parentFile.mkdirs() }

    fun serialize(config: EmceeTestActionConfig) {
        configDumpFile.sink().buffer().use { sink -> configAdapter.toJson(sink, config) }
    }

    fun deserialize(): EmceeTestActionConfig {
        return requireNotNull(configAdapter.fromJson(configDumpFile.source().buffer()))
    }
}
