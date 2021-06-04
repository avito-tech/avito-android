package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal abstract class BuildCacheTestFixture {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir

        File(projectDir, "settings.gradle.kts").writeText(
            buildCacheBlock()
        )
        setupProject(projectDir)
    }

    abstract fun setupProject(projectDir: File)

    private fun buildCacheBlock(): String {
        return """
            fun booleanProperty(name: String, defaultValue: Boolean): Boolean {
                return if (settings.extra.has(name)) {
                    settings.extra[name]?.toString()?.toBoolean() ?: defaultValue
                } else {
                    defaultValue
                }
            }
                
            buildCache {
                local {
                    isEnabled = booleanProperty("localCacheEnabled", false)
                    directory = file(".gradle/build-cache")
                }
                remote<DirectoryBuildCache> {
                    isEnabled = booleanProperty("remoteCacheEnabled", false)
                    directory = file(".gradle/remote-build-cache")
                    isPush = true
                }
            }
            """.trimIndent()
    }

    protected fun clean() {
        File(projectDir, "build").deleteRecursively()
    }

    protected fun build(
        vararg args: String,
        useLocalCache: Boolean = true,
        useRemoteCache: Boolean = true
    ) = BuildMetricsRunner(projectDir)
        .build(
            args.toList()
                .plus("--build-cache")
                .plus("-PlocalCacheEnabled=$useLocalCache")
                .plus("-PremoteCacheEnabled=$useRemoteCache")
        )
}
