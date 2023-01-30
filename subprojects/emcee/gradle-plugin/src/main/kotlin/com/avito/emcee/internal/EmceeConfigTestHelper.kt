package com.avito.emcee.internal

import com.avito.emcee.client.EmceeTestClientConfig
import okio.buffer
import okio.sink
import okio.source
import java.io.File

public class EmceeConfigTestHelper(outputDir: File) {

    private val configAdapter = EmceeTestClientConfig.createMoshiAdapter()

    private val configDumpFile = File(outputDir, "config-dump.json").also { it.parentFile.mkdirs() }

    public fun serialize(config: EmceeTestClientConfig) {
        configDumpFile.sink().buffer().use { sink -> configAdapter.toJson(sink, config) }
    }

    public fun deserialize(): EmceeTestClientConfig {
        return requireNotNull(configAdapter.fromJson(configDumpFile.source().buffer()))
    }
}
