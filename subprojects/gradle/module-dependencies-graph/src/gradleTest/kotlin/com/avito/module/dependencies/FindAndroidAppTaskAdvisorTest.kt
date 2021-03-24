package com.avito.module.dependencies

import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp
import com.avito.module.internal.dependencies.FindAndroidAppTaskAdvisor
import com.avito.module.internal.dependencies.ProjectWithDeps
import com.google.common.truth.Truth.assertThat
import org.gradle.util.Path
import org.junit.jupiter.api.Test

public class FindAndroidAppTaskAdvisorTest {
    private val advisor = FindAndroidAppTaskAdvisor()
    private val root = rootProject()
    private val app = androidApp("app", root)
    private val appSecond = androidApp("appSecond", root)
    private val dependency = javaLib("dependency", root)

    @Test
    public fun `one suitable app`() {
        val verdict = OneSuitableApp(ProjectWithDeps(app, setOf(dependency)))
        val advice = advisor.giveAdvice(verdict)

        assertThat(advice)
            .isEqualTo("In your project is only one suitable app :app")
    }

    @Test
    public fun `multiple suitable apps with equal deps - should prefer both`() {
        val verdict = MultipleSuitableApps(
            setOf(
                ProjectWithDeps(app, setOf(dependency)),
                ProjectWithDeps(appSecond, setOf(dependency))
            )
        )
        val advice = advisor.giveAdvice(verdict)

        assertThat(advice)
            .isEqualTo(
                """
                |There are multiple suitable apps [:app, :appSecond]
                |You should prefer [:app, :appSecond] because they have the least dependencies in graph size=1
            """.trimMargin()
            )
    }

    @Test
    public fun `multiple suitable apps with different deps - should prefer with least deps`() {

        val verdict = MultipleSuitableApps(
            setOf(
                ProjectWithDeps(
                    project = app,
                    dependencies = setOf(dependency)
                ),
                ProjectWithDeps(
                    project = appSecond,
                    dependencies = setOf(dependency, javaLib("dependencySecond", root))
                )
            )
        )
        val advice = advisor.giveAdvice(verdict)

        assertThat(advice)
            .isEqualTo(
                """
                |There are multiple suitable apps [:app, :appSecond]
                |You should prefer :app because it has the least dependencies in graph size=1
            """.trimMargin()
            )
    }

    @Test
    public fun `no suitable apps have equal deps`() {

        val verdict = NoSuitableApps(
            result = setOf(
                NoSuitableApps.Result(
                    project = app,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path))
                ),
                NoSuitableApps.Result(
                    project = appSecond,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path))
                )
            )
        )
        val advice = advisor.giveAdvice(verdict)
        println(advice)
        assertThat(advice)
            .isEqualTo(
                """
                |There are no suitable Android apps
                |Apps are sorted by missing modules:
                |App :app contains [] but missed [:dependency]
                |App :appSecond contains [] but missed [:dependency]
            """.trimMargin()
            )
    }

    @Test
    public fun `no suitable apps one has least deps - choose it`() {
        val verdict = NoSuitableApps(
            result = setOf(
                NoSuitableApps.Result(
                    project = app,
                    dependencies = setOf(Path.path(androidLib("lib", root).path)),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path))
                ),
                NoSuitableApps.Result(
                    project = appSecond,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path))
                )
            )
        )
        val advice = advisor.giveAdvice(verdict)

        assertThat(advice)
            .isEqualTo(
                """
                |There are no suitable Android apps
                |Apps are sorted by missing modules:
                |App :appSecond contains [] but missed [:dependency]
                |App :app contains [] but missed [:dependency]
            """.trimMargin()
            )
    }

    @Test
    public fun `no suitable apps one app has least missing deps - choose it`() {
        val verdict = NoSuitableApps(
            result = setOf(
                NoSuitableApps.Result(
                    project = app,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path), Path.path(androidLib("lib", root).path))
                ),
                NoSuitableApps.Result(
                    project = appSecond,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(Path.path(dependency.path))
                )
            )
        )
        val advice = advisor.giveAdvice(verdict)

        assertThat(advice)
            .isEqualTo(
                """
                |There are no suitable Android apps
                |Apps are sorted by missing modules:
                |App :appSecond contains [] but missed [:dependency]
                |App :app contains [] but missed [:dependency, :lib]
            """.trimMargin()
            )
    }
}
