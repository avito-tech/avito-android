package com.avito.utils.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnvArgsTest {

    @Test
    fun `sandbox build - type carries the sandbox build type`() {
        val project = project("avito.build" to "sandbox", "sandboxBuildType" to "checks")

        val build = project.envArgs.build

        assertThat(build.type).isEqualTo("sandbox-checks")
    }

    @Test
    fun `sandbox build - is sandbox and not local`() {
        val project = project("avito.build" to "sandbox", "sandboxBuildType" to "checks")

        val build = project.envArgs.build

        assertThat(build.isSandbox).isTrue()
        assertThat(build.isLocal).isFalse()
    }

    @Test
    fun `sandbox and local builds - share the build id generated for the process`() {
        val sandbox = project("avito.build" to "sandbox", "sandboxBuildType" to "checks").envArgs.build
        val local = project("avito.build" to "local").envArgs.build

        assertThat(sandbox.id).isEqualTo(local.id)
    }

    @Test
    fun `sandbox build without build type - fails naming the missing property`() {
        val project = project("avito.build" to "sandbox")

        val error = assertThrows<RuntimeException> { project.envArgs.build }

        assertThat(error).hasMessageThat().contains("sandboxBuildType")
    }

    @Test
    fun `local build - type carries the user name`() {
        val project = project("avito.build" to "local")

        val build = project.envArgs.build

        assertThat(build.type).isEqualTo("local-${System.getProperty("user.name")}")
    }

    @Test
    fun `local build - is local and not sandbox`() {
        val project = project("avito.build" to "local")

        val build = project.envArgs.build

        assertThat(build.isLocal).isTrue()
        assertThat(build.isSandbox).isFalse()
    }

    @Test
    fun `teamcity build - carries url and number of the build`() {
        val project = project(
            "avito.build" to "teamcity",
            "teamcityBuildId" to "42",
            "teamcityUrl" to "https://teamcity.example.com",
            "buildNumber" to "7",
            "teamcityBuildType" to "checks",
        )

        val build = project.envArgs.build as EnvArgs.Build.Teamcity

        assertThat(build.url).isEqualTo("https://teamcity.example.com/viewLog.html?buildId=42&tab=buildLog")
        assertThat(build.number).isEqualTo("7")
        assertThat(build.type).isEqualTo("teamcity-checks")
    }

    @Test
    fun `unknown build - fails naming the supported values`() {
        val project = project("avito.build" to "somewhere")

        val error = assertThrows<IllegalStateException> { project.envArgs.build }

        assertThat(error).hasMessageThat().contains("'teamcity', 'local' or 'sandbox'")
    }

    private fun project(vararg properties: Pair<String, String>): Project {
        val project = ProjectBuilder.builder().build()
        properties.forEach { (name, value) -> project.extensions.extraProperties[name] = value }
        return project
    }
}
