@file:Suppress("MaxLineLength")
package com.avito.android.build_verdict

import java.io.File

fun kaptStubGeneratingFails(dir: File) = """
* What went wrong:
Execution failed for task ':app:kaptGenerateStubsDebugKotlin'.
	> Compilation error. See log for more details

* Error logs:
e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration
""".trimIndent()

fun unitTestsFails(dir: File) = """
* What went wrong:
Execution failed for task ':app:testDebugUnitTest'.
	> There were failing tests. See the report at: file://${dir.canonicalPath}/app/build/reports/tests/testDebugUnitTest/index.html

* Error logs:
FAILED tests:
	AppTest.test assert true
	AppTest.test runtime exception
""".trimIndent()

fun kaptFails(dir: File) = """
* What went wrong:
Execution failed for task ':app:kaptDebugKotlin'.
	> A failure occurred while executing org.jetbrains.kotlin.gradle.internal.KaptExecution
		> class java.lang.reflect.InvocationTargetException (no error message)
			> Error while annotation processing

* Error logs:
${dir.canonicalPath}/app/build/tmp/kapt3/stubs/debug/DaggerComponent.java:6: error: [Dagger/MissingBinding] CoffeeMaker cannot be provided without an @Inject constructor or an @Provides-annotated method.
public abstract interface DaggerComponent {
                ^
      CoffeeMaker is requested at
          DaggerComponent.maker()
""".trimIndent()

val customTaskFails = """
* What went wrong:
Execution failed for task ':app:customTask'.
	> Surprise

* Error logs:
Custom verdict
""".trimIndent()

fun compileFails(dir: File) = """
* What went wrong:
Execution failed for task ':app:compileDebugKotlin'.
	> Compilation error. See log for more details

* Error logs:
e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
e: ${dir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration
""".trimIndent()

fun configurationIllegalMethodFails(dir: File) = """
FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
Build file '${dir.canonicalPath}/app/build.gradle' line: 9
A problem occurred evaluating project ':app'.
	> A problem occurred evaluating project ':app'.
		> Could not find method illegal() for arguments [build 'test-project'] on project ':app' of type org.gradle.api.Project.

2: Task failed with an exception.
-----------
A problem occurred configuring project ':app'.
	> A problem occurred configuring project ':app'.
		> compileSdkVersion is not specified. Please add it to build.gradle
""".trimIndent()

fun configurationProjectNotFoundFails(dir: File) = """
FAILURE: Build failed with an exception.

* What went wrong:
Build file '${dir.canonicalPath}/app/build.gradle' line: 9
A problem occurred evaluating project ':app'.
	> A problem occurred evaluating project ':app'.
		> Project with path ':not-existed' could not be found in project ':app'.
""".trimIndent()
