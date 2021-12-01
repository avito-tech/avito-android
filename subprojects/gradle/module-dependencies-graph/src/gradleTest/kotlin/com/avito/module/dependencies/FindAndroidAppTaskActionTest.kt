package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.DependenciesGraphBuilder
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.test.gradle.androidApp
import com.avito.test.gradle.apiDependency
import com.avito.test.gradle.implementationDependency
import com.avito.test.gradle.javaLib
import com.avito.test.gradle.rootProject
import com.google.common.truth.Truth.assertThat
import org.gradle.util.Path.path
import org.junit.jupiter.api.Test

public class FindAndroidAppTaskActionTest {

    private val root = rootProject()
    private val graphBuilder = AndroidAppsGraphBuilder(DependenciesGraphBuilder(root))
    private val action = FindAndroidAppTaskAction(graphBuilder)

    @Test
    public fun `NoSuitableApps - Result contains all no suitable apps`() {
        val rootApp = androidApp("rootApp", root)
        val rootAppSecond = androidApp("rootAppSecond", root)
        val a = javaLib("a", rootApp)
        val b = javaLib("b", rootApp)
        val c = javaLib("c", root)

        rootApp.apiDependency(a)
        a.implementationDependency(b)

        val actual = action.findAppFor(
            modules = setOf(path(c.path)),
            configurations = setOf(ConfigurationType.Main)
        )
        assertThat(actual)
            .isInstanceOf(NoSuitableApps::class.java)

        val noSuitableApps = actual as NoSuitableApps
        val result = noSuitableApps.result.toList()

        assertThat(result.size)
            .isEqualTo(2)

        assertThat(result[0])
            .isEqualTo(
                NoSuitableApps.Result(
                    project = rootApp,
                    dependencies = setOf(path(a.path), path(b.path)),
                    presentedModules = emptySet(),
                    missedModules = setOf(path(c.path))
                )
            )

        assertThat(result[1])
            .isEqualTo(
                NoSuitableApps.Result(
                    project = rootAppSecond,
                    dependencies = emptySet(),
                    presentedModules = emptySet(),
                    missedModules = setOf(path(c.path))
                )
            )
    }
}
