package com.avito.emcee.worker

import com.google.common.truth.Truth
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileReader

internal class ConfigSerializationTest {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(Config::class.java)

    @Test
    fun deserialize(@TempDir temp: File) {
        val file = File(temp, "config.json").apply { createNewFile() }
        file.writeText(
            """
                {
                    "workerPort": 80,
                    "queue": {
                        "url": "http://127.0.0.1:41000",
                        "retriesCount": 3,
                        "retryDelayMs": 10000
                    },
                    "androidSdkPath": "/Users/Shared/Android/sdk",
                    "configurations": [
                        { 
                            "sdk": 22, 
                            "type": "default", 
                            "emulatorFileName": "stub-emulator-name", 
                            "sdCardFileName": "stub-sd-card-name" 
                        },
                        { 
                            "sdk": 31, 
                            "type": "default", 
                            "emulatorFileName": "stub-emulator-name", 
                            "sdCardFileName": "stub-sd-card-name" 
                        }
                    ]
                }
            """.trimIndent()
        )

        val config = deserializeConfig(file)
        Truth.assertThat(config)
            .isEqualTo(
                Config(
                    workerPort = 80,
                    queue = Config.QueueConfig(
                        url = "http://127.0.0.1:41000",
                        retriesCount = 3,
                        retryDelayMs = 10_000
                    ),
                    androidSdkPathString = "/Users/Shared/Android/sdk",
                    configurations = setOf(
                        Config.AvdConfiguration(
                            sdk = 22,
                            type = "default",
                            emulatorFileName = "stub-emulator-name",
                            sdCardFileName = "stub-sd-card-name"
                        ),
                        Config.AvdConfiguration(
                            sdk = 31,
                            type = "default",
                            emulatorFileName = "stub-emulator-name",
                            sdCardFileName = "stub-sd-card-name"
                        )
                    )
                )
            )
    }

    private fun deserializeConfig(file: File): Config {
        val fileReader = FileReader(file)
        val json = fileReader.readText()
        return requireNotNull(adapter.fromJson(json)) {
            "Failed to deserialize config $json"
        }
    }
}
