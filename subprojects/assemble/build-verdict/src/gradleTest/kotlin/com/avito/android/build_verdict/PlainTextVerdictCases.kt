@file:Suppress("MaxLineLength")

package com.avito.android.build_verdict

import java.io.File

internal object PlainTextVerdictCases {

    class Execution(private val dir: File) : VerdictCases.Execution {

        override fun compileKotlinFails() = """
            |* What went wrong:
            |Execution failed for task ':app:compileDebugKotlin'.
            |	> Compilation error. See log for more details
            |
            |* Error logs:
            |e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
            |e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration
            """.trimMargin()

        override fun customTaskFails() = """
            |* What went wrong:
            |Execution failed for task ':customTask'.
            |	> Surprise
            |
            |* Task result:
            |User added verdict: https://www.google.com/
            |
            |* Error logs:
            |No error logs
            """.trimMargin()

        override fun buildVerdictTaskFails() = """
            |* What went wrong:
            |Execution failed for task ':customTask'.
            |	> Surprise
            |
            |* Task result:
            |Custom verdict
            |User added verdict: https://www.google.com/
            |
            |* Error logs:
            |No error logs
            """.trimMargin()

        override fun kaptFails() = """
            |* What went wrong:
            |Execution failed for task ':app:kaptDebugKotlin'.
            |	> A failure occurred while executing org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask${'$'}KaptExecutionWorkAction
            |		> class java.lang.reflect.InvocationTargetException (no error message)
            |			> Error while annotation processing
            |
            |* Error logs:
            |${dir.canonicalPath}/app/build/tmp/kapt3/stubs/debug/DaggerComponent.java:6: error: [Dagger/MissingBinding] CoffeeMaker cannot be provided without an @Inject constructor or an @Provides-annotated method.
            |public abstract interface DaggerComponent {
            |                ^
            |      CoffeeMaker is requested at
            |          DaggerComponent.maker()
            """.trimMargin()

        override fun kaptStubGeneratingFails() = """
            |* What went wrong:
            |Execution failed for task ':app:kaptGenerateStubsDebugKotlin'.
            |	> Compilation error. See log for more details
            |
            |* Error logs:
            |e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
            |e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration
            """.trimMargin()

        override fun unitTestsFails() = """
            |* What went wrong:
            |Execution failed for task ':app:testDebugUnitTest'.
            |	> There were failing tests. See the report at: file://${dir.canonicalPath}/app/build/reports/tests/testDebugUnitTest/index.html
            |
            |* Task result:
            |FAILED tests:
            |	AppTest.test assert true
            |	AppTest.test runtime exception
            |
            |* Error logs:
            |No error logs
            """.trimMargin()
    }

    class Configuration(private val dir: File) : VerdictCases.Configuration {

        override fun wrongProjectDependencyFails() = """
            |FAILURE: Build failed with an exception.
            |
            |* What went wrong:
            |Build file '${dir.canonicalPath}/app/build.gradle' line: 9
            |A problem occurred evaluating project ':app'.
            |	> A problem occurred evaluating project ':app'.
            |		> Project with path ':not-existed' could not be found in project ':app'.
            """.trimMargin()

        override fun illegalMethodFails() = """
            |FAILURE: Build completed with 2 failures.
            |
            |1: Task failed with an exception.
            |-----------
            |Build file '${dir.canonicalPath}/app/build.gradle' line: 7
            |A problem occurred evaluating project ':app'.
            |	> A problem occurred evaluating project ':app'.
            |		> Could not find method illegal() for arguments [build 'test-project'] on project ':app' of type org.gradle.api.Project.
            """.trimMargin()
    }
}
