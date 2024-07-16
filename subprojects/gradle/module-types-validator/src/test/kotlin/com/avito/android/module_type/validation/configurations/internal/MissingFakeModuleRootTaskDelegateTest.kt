package com.avito.android.module_type.validation.configurations.internal

import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingFakeModuleRootTaskDelegate
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MissingFakeModuleRootTaskDelegateTest {

    @Test
    fun `no problems -- no error message`() {
        val result = MissingFakeModuleRootTaskDelegate().validate(
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITHOUT_PROBLEMS,
            ignoreLogicalModuleRegexesText = INPUT_IGNORE_LOGICAL_MODULE_REGEXES_TEXT,
        )
        assertThat(result).isEqualTo(Result.success(Unit))
    }

    @Test
    fun `three problems, one ignored -- two modules reported`() {
        val result = MissingFakeModuleRootTaskDelegate().validate(
            projectsTaskOutputText = INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITH_PROBLEMS,
            ignoreLogicalModuleRegexesText = INPUT_IGNORE_LOGICAL_MODULE_REGEXES_TEXT,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()!!.message).isEqualTo(
            """
                The following logical modules have :public and :impl modules,
                but do not have :fake modules. Create :fake modules for them.
                See docs for details: https://links.k.avito.ru/android-missing-fake-modules
                
                :avito:feature-six
                :avito:feature-three
            """.trimIndent()
        )
    }

    companion object {
        private val INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITHOUT_PROBLEMS = """
            ------------------------------------------------------------
            Root project 'avito-android'
            ------------------------------------------------------------
            
            Root project 'avito-android'
            +--- Project ':avito'
            |    +--- Project ':avito:feature-one'
            |    |    +--- Project ':avito:feature-one:fake'
            |    |    +--- Project ':avito:feature-one:impl'
            |    |    \--- Project ':avito:feature-one:public'
            |    +--- Project ':avito:feature-two'
            |    |    +--- Project ':avito:feature-two:abstract'
            |    |    \--- Project ':avito:feature-two:public'
            |    +--- Project ':avito:feature-four'
            |    |    +--- Project ':avito:feature-four:fake'
            |    |    \--- Project ':avito:feature-four:public'
            |    \--- Project ':avito:feature-five'
            |    |    +--- Project ':avito:feature-five:debug'
            |    |    \--- Project ':avito:feature-five:public'
            \--- Project ':platform'
            
            Included builds
            +--- Included build ':build-logic-settings'
            \--- Included build ':build-logic'
            
            To see a list of the tasks of a project, run gradlew <project-path>:tasks
            For example, try running gradlew :common:tasks
        """.trimIndent()

        private val INPUT_PROJECTS_TASK_OUTPUT_TEXT_WITH_PROBLEMS = """
            ------------------------------------------------------------
            Root project 'avito-android'
            ------------------------------------------------------------
            
            Root project 'avito-android'
            +--- Project ':avito'
            |    +--- Project ':avito:feature-one'
            |    |    +--- Project ':avito:feature-one:fake'
            |    |    +--- Project ':avito:feature-one:impl'
            |    |    \--- Project ':avito:feature-one:public'
            |    +--- Project ':avito:feature-two'
            |    |    +--- Project ':avito:feature-two:abstract'
            |    |    \--- Project ':avito:feature-two:public'
            |    +--- Project ':avito:feature-three'
            |    |    +--- Project ':avito:feature-three:impl'
            |    |    \--- Project ':avito:feature-three:public'
            |    +--- Project ':avito:feature-four'
            |    |    +--- Project ':avito:feature-four:fake'
            |    |    \--- Project ':avito:feature-four:public'
            |    +--- Project ':avito:feature-five'
            |    |    +--- Project ':avito:feature-five:debug'
            |    |    \--- Project ':avito:feature-five:public'
            |    \--- Project ':avito:feature-six'
            |    |    +--- Project ':avito:feature-six:debug'
            |    |    +--- Project ':avito:feature-six:impl'
            |    |    \--- Project ':avito:feature-six:public'
            +--- Project ':not-avito'
            |    \--- Project ':not-avito:feature-six'
            |    |    +--- Project ':not-avito:feature:impl'
            |    |    \--- Project ':not-avito:feature:public'
            \--- Project ':platform'
            
            Included builds
            +--- Included build ':build-logic-settings'
            \--- Included build ':build-logic'
            
            To see a list of the tasks of a project, run gradlew <project-path>:tasks
            For example, try running gradlew :common:tasks
        """.trimIndent()

        private val INPUT_IGNORE_LOGICAL_MODULE_REGEXES_TEXT = """
            :not-avito:.*
        """.trimIndent()
    }
}
