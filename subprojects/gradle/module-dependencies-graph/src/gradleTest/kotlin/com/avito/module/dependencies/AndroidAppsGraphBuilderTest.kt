package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.DependenciesGraphBuilder
import com.avito.module.internal.dependencies.ProjectConfigurationNode
import com.avito.module.internal.dependencies.ProjectWithDeps
import com.avito.test.gradle.androidApp
import com.avito.test.gradle.androidLib
import com.avito.test.gradle.rootProject
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.Project
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class AndroidAppsGraphBuilderTest {

    private lateinit var root: Project

    @BeforeEach
    fun before() {
        root = rootProject()

        val appA = androidApp("appA", parent = root)
        val appB = androidApp("appB", parent = root)

        val nodeC = androidLib("nodeC", parent = root)
        val nodeCtest = androidLib("nodeCtest", parent = root)
        nodeCtest.dependencies.add("implementation", nodeC)

        val nodeD = androidLib("nodeD", parent = root)
        val nodeE = androidLib("nodeE", parent = root)

        val leafF = androidLib("leafF", parent = root)
        val leafG = androidLib("leafG", parent = root)

        nodeD.dependencies.add("implementation", leafF)

        nodeE.dependencies.add("implementation", leafF)
        nodeE.dependencies.add("implementation", leafG)

        appA.dependencies.add("implementation", nodeC)
        appA.dependencies.add("androidTestImplementation", nodeCtest)
        appA.dependencies.add("implementation", nodeD)

        appB.dependencies.add("implementation", nodeE)
    }

    @Test
    fun `dependencies - main configuration`() {
        val apps = graphBuilder()
            .buildDependenciesGraph(ConfigurationType.Main)

        assertThat(apps).hasSize(2)

        apps.findOrThrow(":appA").also { app ->
            val appDeps = app.directDependencies()
            assertThat(appDeps).hasSize(2)

            val nodeC = appDeps.findOrThrow(":nodeC")
            assertThat(nodeC.directDependencies()).isEmpty()

            val nodeD = appDeps.findOrThrow(":nodeD")
            val nodeDdeps = nodeD.directDependencies()
            assertThat(nodeDdeps).hasSize(1)

            val leafF = nodeDdeps.findOrThrow(":leafF")
            assertThat(leafF.directDependencies()).isEmpty()
        }
        apps.findOrThrow(":appB").also { app ->
            val appDeps = app.directDependencies()
            assertThat(appDeps).hasSize(1)

            val nodeE = appDeps.findOrThrow(":nodeE")
            val nodeEdeps = nodeE.directDependencies()
            assertThat(nodeEdeps).hasSize(2)

            val leafF = nodeEdeps.findOrThrow(":leafF")
            assertThat(leafF.directDependencies()).isEmpty()

            val leafG = nodeEdeps.findOrThrow(":leafG")
            assertThat(leafG.directDependencies()).isEmpty()
        }
    }

    @Test
    fun `dependencies - androidTest configuration`() {
        val apps = graphBuilder()
            .buildDependenciesGraph(ConfigurationType.AndroidTests)

        assertThat(apps).hasSize(2)

        apps.findOrThrow(":appA").also { app ->
            val appDeps = app.directDependencies()
            assertThat(appDeps).hasSize(1)

            val nodeCtest = appDeps.findOrThrow(":nodeCtest")
            val nodeCtestDeps = nodeCtest.directDependencies()
            assertThat(nodeCtestDeps).hasSize(1)

            val nodeC = nodeCtestDeps.findOrThrow(":nodeC")
            assertWithMessage("Avoid cycles to test module")
                .that(nodeC.directDependencies()).isEmpty()
        }
        apps.findOrThrow(":appB").also { app ->
            assertThat(app.directDependencies()).isEmpty()
        }
    }

    @Test
    fun `all dependencies - main configuration`() {
        val apps = graphBuilder()
            .buildDependenciesGraph(ConfigurationType.Main)

        assertThat(apps).hasSize(2)
        apps.findOrThrow(":appA").also { app ->
            val dependencies = app.allDependencies().map { it.path }
            assertThat(dependencies).containsExactly(":nodeC", ":nodeD", ":leafF")
        }
        apps.findOrThrow(":appB").also { app ->
            val dependencies = app.allDependencies().map { it.path }
            assertThat(dependencies).containsExactly(":nodeE", ":leafF", ":leafG")
        }
    }

    @Test
    fun `all dependencies - androidTest configuration`() {
        val apps = graphBuilder()
            .buildDependenciesGraph(ConfigurationType.AndroidTests)

        assertThat(apps).hasSize(2)
        apps.findOrThrow(":appA").also { app ->
            val dependencies = app.allDependencies().map { it.path }
            assertThat(dependencies).containsExactly(
                ":nodeCtest", // direct dependency
                ":nodeC" // transitive dependency
            )
        }
        apps.findOrThrow(":appB").also { app ->
            assertThat(app.allDependencies()).isEmpty()
        }
    }

    private fun List<ProjectWithDeps>.findOrThrow(path: String): ProjectWithDeps {
        return requireNotNull(find { it.project.path == path }) {
            fail("$this doesn't contain $path")
        }
    }

    private fun Set<ProjectConfigurationNode>.findOrThrow(path: String):
        ProjectConfigurationNode {
        return requireNotNull(find { it.project.path == path }) {
            fail("$this doesn't contain $path")
        }
    }

    private fun graphBuilder() = AndroidAppsGraphBuilder(
        DependenciesGraphBuilder(root)
    )
}
