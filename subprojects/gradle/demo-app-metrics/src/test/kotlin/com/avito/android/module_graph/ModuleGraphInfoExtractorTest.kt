package com.avito.android.module_graph

import StubApplication
import com.avito.android.module_graph.extractor.ModuleGraphInfoExtractor
import com.avito.android.module_graph.models.GradleDependency
import com.avito.android.module_graph.models.ModuleGraphEdge
import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.module_graph.models.ModuleLinesOfCode
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ModuleGraphInfoExtractorTest {

    @Test
    fun `user app, feature, internal lib - extract info`() {
        val moduleGraphInfoExtractor = ModuleGraphInfoExtractor(
            dependencies = listOf(
                GradleDependency(
                    from = ":avito-app",
                    to = ":avito:feature-1",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito:feature-1",
                    to = ":avito:lib-1",
                    type = GradleDependency.Type.Api,
                )
            ),
            modulesToModuleTypes = mapOf(
                ":avito-app" to ModuleType(StubApplication, FunctionalType.UserApp),
                ":avito:feature-1" to ModuleType(StubApplication, FunctionalType.Impl),
                ":avito:lib-1" to ModuleType(StubApplication, FunctionalType.Public),
            ),
            linesOfCodeCounter = StubModuleLinesOfCodeCounter,
            projectDir = File("avito-android")
        )

        val actualInfo = moduleGraphInfoExtractor.extractInfo()

        assertThat(actualInfo).isEqualTo(
            ModuleGraphInfo(
                dependencies = listOf(
                    ModuleGraphEdge(
                        from = ":avito-app",
                        to = ":avito:feature-1",
                        type = "Implementation",
                    ),
                    ModuleGraphEdge(
                        from = ":avito:feature-1",
                        to = ":avito:lib-1",
                        type = "Api",
                    )
                ),
                applications = listOf(":avito-app"),
                linesOfCode = mapOf(
                    ":avito-app" to ModuleLinesOfCode(main = 10, test = 1, androidTest = 1),
                    ":avito:feature-1" to ModuleLinesOfCode(main = 10),
                    ":avito:lib-1" to ModuleLinesOfCode(main = 10),
                ),
                transitiveLinesOfCode = mapOf(
                    ":avito-app" to 32,
                    ":avito:feature-1" to 20,
                    ":avito:lib-1" to 10,
                ),
                impactedApplications = mapOf(
                    ":avito-app" to listOf(":avito-app"),
                    ":avito:feature-1" to listOf(":avito-app"),
                    ":avito:lib-1" to listOf(":avito-app"),
                )
            )
        )
    }

    @Test
    fun `user app, demo app, feature, internal lib - extract info`() {
        val moduleGraphInfoExtractor = ModuleGraphInfoExtractor(
            dependencies = listOf(
                GradleDependency(
                    from = ":avito-app",
                    to = ":avito:feature-1",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito:demo-1",
                    to = ":avito:feature-1",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito:feature-1",
                    to = ":avito:lib-1",
                    type = GradleDependency.Type.Api,
                )
            ),
            modulesToModuleTypes = mapOf(
                ":avito-app" to ModuleType(StubApplication, FunctionalType.UserApp),
                ":avito:demo-1" to ModuleType(StubApplication, FunctionalType.DemoApp),
                ":avito:feature-1" to ModuleType(StubApplication, FunctionalType.Impl),
                ":avito:lib-1" to ModuleType(StubApplication, FunctionalType.Public),
            ),
            linesOfCodeCounter = StubModuleLinesOfCodeCounter,
            projectDir = File("avito-android")
        )

        val actualInfo = moduleGraphInfoExtractor.extractInfo()

        assertThat(actualInfo).isEqualTo(
            ModuleGraphInfo(
                dependencies = listOf(
                    ModuleGraphEdge(
                        from = ":avito-app",
                        to = ":avito:feature-1",
                        type = "Implementation",
                    ),
                    ModuleGraphEdge(
                        from = ":avito:demo-1",
                        to = ":avito:feature-1",
                        type = "Implementation",
                    ),
                    ModuleGraphEdge(
                        from = ":avito:feature-1",
                        to = ":avito:lib-1",
                        type = "Api",
                    )
                ),
                applications = listOf(":avito-app", ":avito:demo-1"),
                linesOfCode = mapOf(
                    ":avito-app" to ModuleLinesOfCode(main = 10, test = 1, androidTest = 1),
                    ":avito:demo-1" to ModuleLinesOfCode(main = 10, test = 1, androidTest = 1),
                    ":avito:feature-1" to ModuleLinesOfCode(main = 10),
                    ":avito:lib-1" to ModuleLinesOfCode(main = 10),
                ),
                transitiveLinesOfCode = mapOf(
                    ":avito-app" to 32,
                    ":avito:demo-1" to 32,
                    ":avito:feature-1" to 20,
                    ":avito:lib-1" to 10,
                ),
                impactedApplications = mapOf(
                    ":avito-app" to listOf(":avito-app"),
                    ":avito:demo-1" to listOf(":avito:demo-1"),
                    ":avito:feature-1" to listOf(":avito-app", ":avito:demo-1"),
                    ":avito:lib-1" to listOf(":avito-app", ":avito:demo-1"),
                )
            )
        )
    }

    @Test
    fun `user app, feature, external lib - extract info`() {
        val moduleGraphInfoExtractor = ModuleGraphInfoExtractor(
            dependencies = listOf(
                GradleDependency(
                    from = ":avito-app",
                    to = ":avito:feature-1",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito:feature-1",
                    to = "some-external-lib",
                    type = GradleDependency.Type.Api,
                )
            ),
            modulesToModuleTypes = mapOf(
                ":avito-app" to ModuleType(StubApplication, FunctionalType.UserApp),
                ":avito:feature-1" to ModuleType(StubApplication, FunctionalType.Impl),
            ),
            linesOfCodeCounter = StubModuleLinesOfCodeCounter,
            projectDir = File("avito-android")
        )

        val actualInfo = moduleGraphInfoExtractor.extractInfo()

        assertThat(actualInfo).isEqualTo(
            ModuleGraphInfo(
                dependencies = listOf(
                    ModuleGraphEdge(
                        from = ":avito-app",
                        to = ":avito:feature-1",
                        type = "Implementation",
                    ),
                ),
                applications = listOf(":avito-app"),
                linesOfCode = mapOf(
                    ":avito-app" to ModuleLinesOfCode(main = 10, test = 1, androidTest = 1),
                    ":avito:feature-1" to ModuleLinesOfCode(main = 10),
                ),
                transitiveLinesOfCode = mapOf(
                    ":avito-app" to 22,
                    ":avito:feature-1" to 10,
                ),
                impactedApplications = mapOf(
                    ":avito-app" to listOf(":avito-app"),
                    ":avito:feature-1" to listOf(":avito-app"),
                )
            )
        )
    }

    @Test
    fun `diamond dependency - extract info`() {
        val moduleGraphInfoExtractor = ModuleGraphInfoExtractor(
            dependencies = listOf(
                GradleDependency(
                    from = ":avito-app",
                    to = ":avito:feature-1",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito-app",
                    to = ":avito:feature-2",
                    type = GradleDependency.Type.Implementation,
                ),
                GradleDependency(
                    from = ":avito:feature-1",
                    to = ":avito:lib-1",
                    type = GradleDependency.Type.Api,
                ),
                GradleDependency(
                    from = ":avito:feature-2",
                    to = ":avito:lib-1",
                    type = GradleDependency.Type.Api,
                ),
            ),
            modulesToModuleTypes = mapOf(
                ":avito-app" to ModuleType(StubApplication, FunctionalType.UserApp),
                ":avito:feature-1" to ModuleType(StubApplication, FunctionalType.Impl),
                ":avito:feature-2" to ModuleType(StubApplication, FunctionalType.Impl),
                ":avito:lib-1" to ModuleType(StubApplication, FunctionalType.Impl),
            ),
            linesOfCodeCounter = StubModuleLinesOfCodeCounter,
            projectDir = File("avito-android")
        )

        val actualInfo = moduleGraphInfoExtractor.extractInfo()

        assertThat(actualInfo).isEqualTo(
            ModuleGraphInfo(
                dependencies = listOf(
                    ModuleGraphEdge(
                        from = ":avito-app",
                        to = ":avito:feature-1",
                        type = "Implementation",
                    ),
                    ModuleGraphEdge(
                        from = ":avito-app",
                        to = ":avito:feature-2",
                        type = "Implementation",
                    ),
                    ModuleGraphEdge(
                        from = ":avito:feature-1",
                        to = ":avito:lib-1",
                        type = "Api",
                    ),
                    ModuleGraphEdge(
                        from = ":avito:feature-2",
                        to = ":avito:lib-1",
                        type = "Api",
                    ),
                ),
                applications = listOf(":avito-app"),
                linesOfCode = mapOf(
                    ":avito-app" to ModuleLinesOfCode(main = 10, test = 1, androidTest = 1),
                    ":avito:feature-1" to ModuleLinesOfCode(main = 10),
                    ":avito:feature-2" to ModuleLinesOfCode(main = 10),
                    ":avito:lib-1" to ModuleLinesOfCode(main = 10),
                ),
                transitiveLinesOfCode = mapOf(
                    ":avito-app" to 42,
                    ":avito:feature-1" to 20,
                    ":avito:feature-2" to 20,
                    ":avito:lib-1" to 10,
                ),
                impactedApplications = mapOf(
                    ":avito-app" to listOf(":avito-app"),
                    ":avito:feature-1" to listOf(":avito-app"),
                    ":avito:feature-2" to listOf(":avito-app"),
                    ":avito:lib-1" to listOf(":avito-app"),
                )
            )
        )
    }
}
