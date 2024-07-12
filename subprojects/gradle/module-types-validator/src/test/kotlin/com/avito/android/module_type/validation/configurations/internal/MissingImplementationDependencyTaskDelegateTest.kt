package com.avito.android.module_type.validation.configurations.internal

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingImplementationDependencyTaskDelegate
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MissingImplementationDependencyTaskDelegateTest {

    @Test
    fun `user app -- no Impl for Public -- success`() {
        val result = MissingImplementationDependencyTaskDelegate().validate(
            appModulePath = INPUT_APP_MODULE_PATH,
            appModuleBuildFilePath = INPUT_APP_MODULE_BUILD_FILE_PATH,
            appModuleType = FunctionalType.UserApp,
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITHOUT_IMPLS,
            appDependenciesText = INPUT_APP_DEPENDENCIES,
        )
        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `demo app -- no Fake for Public -- success`() {
        val result = MissingImplementationDependencyTaskDelegate().validate(
            appModulePath = INPUT_APP_MODULE_PATH,
            appModuleBuildFilePath = INPUT_APP_MODULE_BUILD_FILE_PATH,
            appModuleType = FunctionalType.DemoApp,
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITHOUT_IMPLS,
            appDependenciesText = INPUT_APP_DEPENDENCIES,
        )
        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `user app -- two Impls for Public -- error message with both`() {
        val result = MissingImplementationDependencyTaskDelegate().validate(
            appModulePath = INPUT_APP_MODULE_PATH,
            appModuleBuildFilePath = INPUT_APP_MODULE_BUILD_FILE_PATH,
            appModuleType = FunctionalType.UserApp,
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITH_IMPLS,
            appDependenciesText = INPUT_APP_DEPENDENCIES,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("""

            Please add the following dependencies to avito/feature-one/demo/build.gradle
            
                implementation(
                    project(":avito:feature-two:impl"), // or project(":avito:feature-two:impl-two"),
                )
            
            Dependency for :avito:feature-two:public appears from: 
                :avito:feature-one:demo -> :avito:feature-one:impl -> :avito:feature-two:public
            
        """.trimIndent())
    }

    @Test
    fun `demo app -- one Fake for Public -- error message with this Fake`() {
        val result = MissingImplementationDependencyTaskDelegate().validate(
            appModulePath = INPUT_APP_MODULE_PATH,
            appModuleBuildFilePath = INPUT_APP_MODULE_BUILD_FILE_PATH,
            appModuleType = FunctionalType.DemoApp,
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITH_IMPLS,
            appDependenciesText = INPUT_APP_DEPENDENCIES,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("""
            
            Please add the following dependencies to avito/feature-one/demo/build.gradle
            
                implementation(
                    project(":avito:feature-two:fake"),
                )
            
            Dependency for :avito:feature-two:public appears from: 
                :avito:feature-one:demo -> :avito:feature-one:impl -> :avito:feature-two:public

        """.trimIndent())
    }

    companion object {
        private const val INPUT_APP_MODULE_PATH = ":avito:feature-one:demo"

        private const val INPUT_APP_MODULE_BUILD_FILE_PATH = "avito/feature-one/demo/build.gradle"

        private val INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITHOUT_IMPLS = """
            ------------------------------------------------------------
            Root project 'avito-android'
            ------------------------------------------------------------
            
            Root project 'avito-android'
            +--- Project ':avito'
            |    +--- Project ':avito:feature-one'
            |    |    +--- Project ':avito:feature-one:fake'
            |    |    +--- Project ':avito:feature-one:impl'
            |    |    \--- Project ':avito:feature-one:public'
            |    \--- Project ':avito:feature-two'
            |    |    +--- Project ':avito:feature-two:abstract'
            |    |    \--- Project ':avito:feature-two:public'
            \--- Project ':platform'
            
            Included builds
            +--- Included build ':build-logic-settings'
            \--- Included build ':build-logic'
            
            To see a list of the tasks of a project, run gradlew <project-path>:tasks
            For example, try running gradlew :common:tasks
        """.trimIndent()

        private val INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITH_IMPLS = """
            ------------------------------------------------------------
            Root project 'avito-android'
            ------------------------------------------------------------
            
            Root project 'avito-android'
            +--- Project ':avito'
            |    +--- Project ':avito:feature-one'
            |    |    +--- Project ':avito:feature-one:fake'
            |    |    +--- Project ':avito:feature-one:impl'
            |    |    \--- Project ':avito:feature-one:public'
            |    \--- Project ':avito:feature-two'
            |    |    +--- Project ':avito:feature-two:abstract'
            |    |    +--- Project ':avito:feature-two:fake'
            |    |    +--- Project ':avito:feature-two:impl'
            |    |    +--- Project ':avito:feature-two:impl-two'
            |    |    \--- Project ':avito:feature-two:public'
            \--- Project ':platform'
            
            Included builds
            +--- Included build ':build-logic-settings'
            \--- Included build ':build-logic'
            
            To see a list of the tasks of a project, run gradlew <project-path>:tasks
            For example, try running gradlew :common:tasks
        """.trimIndent()

        private val INPUT_APP_DEPENDENCIES = """
            ------------------------------------------------------------
            Project ':lib-c:demo'
            ------------------------------------------------------------

            apiDependenciesMetadata
            No dependencies

            implementationDependenciesMetadata
            +--- project :avito:feature-one:impl
            |    +--- project :avito:feature-one:public
            |    \--- project :avito:feature-two:public
            |         \--- project :avito:feature-two:abstract
            \--- org.jetbrains.kotlin:kotlin-stdlib:1.7.10
                 +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.7.10
                 \--- org.jetbrains:annotations:13.0

            (*) - dependencies omitted (listed previously)

            A web-based, searchable dependency report is available by adding the --scan option.
        """.trimIndent()
    }
}
