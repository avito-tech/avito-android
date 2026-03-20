package com.avito.android.module_type.validation.configurations.internal

import com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies.internal.ForbiddenDemoDependenciesTaskDelegate
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ForbiddenDemoDependenciesTaskDelegateTest {

    @Test
    fun `no forbidden dependencies in project - success`() {
        val result = ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = ":feature:demo",
            appModuleBuildFilePath = "feature/demo/build.gradle",
            appDependenciesText = INPUT_APP_DEPENDENCIES_WITHOUT_FORBIDDEN,
            forbiddenDependenciesText = ":heavy-module",
            allowedDependencies = emptySet(),
        )

        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `forbidden dependency is allowed - success`() {
        val result = ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = ":feature:demo",
            appModuleBuildFilePath = "feature/demo/build.gradle",
            appDependenciesText = INPUT_APP_DEPENDENCIES_WITH_FORBIDDEN,
            forbiddenDependenciesText = ":heavy-module",
            allowedDependencies = setOf(":heavy-module"),
        )

        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `forbidden dependency found - error message with dependency chain`() {
        val result = ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = ":feature:demo",
            appModuleBuildFilePath = "feature/demo/build.gradle",
            appDependenciesText = INPUT_APP_DEPENDENCIES_WITH_FORBIDDEN,
            forbiddenDependenciesText = ":heavy-module",
            allowedDependencies = emptySet(),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("""
            :feature:demo depends on forbidden modules.

            See docs for details: https://links.k.avito.ru/android-forbidden-demo-dependencies

            Forbidden modules:
                :heavy-module

            Dependency for :heavy-module appears from:
                :feature:demo -> :feature:impl -> :heavy-module

        """.trimIndent())
    }

    @Test
    fun `unused allow found - error message with build file path`() {
        val result = ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = ":feature:demo",
            appModuleBuildFilePath = "feature/demo/build.gradle",
            appDependenciesText = INPUT_APP_DEPENDENCIES_WITHOUT_FORBIDDEN,
            forbiddenDependenciesText = ":heavy-module",
            allowedDependencies = setOf(":heavy-module"),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("""
            Some allow() entries in feature/demo/build.gradle are excessive. Remove them:
                allow(":heavy-module")

        """.trimIndent())
    }

    @Test
    fun `allow not in forbidden list - error message with build file path`() {
        val result = ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = ":feature:demo",
            appModuleBuildFilePath = "feature/demo/build.gradle",
            appDependenciesText = INPUT_APP_DEPENDENCIES_WITH_FORBIDDEN,
            forbiddenDependenciesText = ":other-heavy-module",
            allowedDependencies = setOf(":heavy-module"),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("""
            Some allow() entries in feature/demo/build.gradle are excessive. Remove them:
                allow(":heavy-module")

        """.trimIndent())
    }

    private companion object {
        val INPUT_APP_DEPENDENCIES_WITHOUT_FORBIDDEN = """
            ------------------------------------------------------------
            Project ':feature:demo'
            ------------------------------------------------------------

            implementationDependenciesMetadata
            \--- project :feature:impl
                 \--- project :feature:public

            (*) - dependencies omitted (listed previously)

            A web-based, searchable dependency report is available by adding the --scan option.
        """.trimIndent()

        val INPUT_APP_DEPENDENCIES_WITH_FORBIDDEN = """
            ------------------------------------------------------------
            Project ':feature:demo'
            ------------------------------------------------------------

            implementationDependenciesMetadata
            \--- project :feature:impl
                 +--- project :feature:public
                 \--- project :heavy-module

            (*) - dependencies omitted (listed previously)

            A web-based, searchable dependency report is available by adding the --scan option.
        """.trimIndent()
    }
}
