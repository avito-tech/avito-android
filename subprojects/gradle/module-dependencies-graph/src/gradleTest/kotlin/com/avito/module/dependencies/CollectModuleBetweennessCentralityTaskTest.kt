package com.avito.module.dependencies

import com.avito.module.metrics.CollectModuleBetweennessCentralityTask.Companion.OUTPUT_BETWEENNESS_CENTRALITY_PATH
import com.avito.module.metrics.CollectModuleBetweennessCentralityTask.Companion.OUTPUT_GRAPH_PATH
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CollectModuleBetweennessCentralityTaskTest {

    @Test
    fun `test computation`(@TempDir dir: File) {
        DependenciesGraphTestProject.generate(dir)

        gradlew(dir, "collectModuleBetweennessCentrality", configurationCache = false)

        Assertions.assertEquals(
            """
                from,to
                :CopyRootB,:NodeC
                :FakeRoot,:NodeC
                :NodeC,:NodeD
                :NodeD,:LeafE
                :NodeG,:LeafE
                :RootA,:LeafE
                :RootA,:LeafF
                :RootB,:NodeC

            """.trimIndent(),
            dir.resolve("build").resolve(OUTPUT_GRAPH_PATH).readText()
        )

        Assertions.assertEquals(
            """
                module,betweenness-centrality,owners
                :NodeC,6.0,
                :NodeD,4.0,
                :CopyRootB,0.0,
                :FakeRoot,0.0,
                :LeafE,0.0,
                :LeafF,0.0,
                :NodeG,0.0,
                :RootA,0.0,
                :RootB,0.0,
                :UniqueA,0.0,
                :UniqueB,0.0,
                :UniqueCopyB,0.0,

            """.trimIndent(),
            dir.resolve("build").resolve(OUTPUT_BETWEENNESS_CENTRALITY_PATH).readText()
        )
    }
}
