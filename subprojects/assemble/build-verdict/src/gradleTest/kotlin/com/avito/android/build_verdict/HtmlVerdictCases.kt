@file:Suppress("MaxLineLength")

package com.avito.android.build_verdict

import java.io.File

internal object HtmlVerdictCases {

    class Execution(private val tempDir: File) : VerdictCases.Execution {

        override fun compileKotlinFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':app:compileDebugKotlin'.
	&gt; Compilation error. See log for more details
</pre>
    <h3>Error logs:</h3>
    <pre class="logs">e: ${tempDir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
e: ${tempDir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration</pre>
  </body>
</html>""".trimIndent()

        override fun customTaskFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':customTask'.
	&gt; Surprise
</pre>
    <h3>Task verdict:</h3>
    <pre><a href="https://www.google.com/" target="_blank">User added verdict</a></pre>
    <h3>Error logs:</h3>
    <pre class="logs">No error logs</pre>
  </body>
</html>
""".trimIndent()

        override fun buildVerdictTaskFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':customTask'.
	&gt; Surprise
</pre>
    <h3>Task verdict:</h3>
    <pre>Custom verdict
<a href="https://www.google.com/" target="_blank">User added verdict</a></pre>
    <h3>Error logs:</h3>
    <pre class="logs">No error logs</pre>
  </body>
</html>
""".trimIndent()

        override fun kaptFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':app:kaptDebugKotlin'.
	&gt; A failure occurred while executing org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask${'$'}KaptExecutionWorkAction
		&gt; class java.lang.reflect.InvocationTargetException (no error message)
			&gt; Error while annotation processing
</pre>
    <h3>Error logs:</h3>
    <pre class="logs">${tempDir.canonicalPath}/app/build/tmp/kapt3/stubs/debug/DaggerComponent.java:6: error: [Dagger/MissingBinding] CoffeeMaker cannot be provided without an @Inject constructor or an @Provides-annotated method.
public abstract interface DaggerComponent {
                ^
      CoffeeMaker is requested at
          DaggerComponent.maker()</pre>
  </body>
</html>""".trimIndent()

        override fun kaptStubGeneratingFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':app:kaptGenerateStubsDebugKotlin'.
	&gt; Compilation error. See log for more details
</pre>
    <h3>Error logs:</h3>
    <pre class="logs">e: ${tempDir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration
e: ${tempDir.canonicalPath}/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration</pre>
  </body>
</html>""".trimIndent()

        override fun unitTestsFails() = """
<html>
  <head>
    <title>BuildFailed</title>
    <style>.logs {
    color: red;
}</style>
  </head>
  <body>
    <h2>What went wrong:</h2>
    <pre>Execution failed for task ':app:testDebugUnitTest'.
	&gt; There were failing tests. See the report at: file://${tempDir.canonicalPath}/app/build/reports/tests/testDebugUnitTest/index.html
</pre>
    <h3>Task verdict:</h3>
    <pre>FAILED tests:
	AppTest.test assert true
	AppTest.test runtime exception</pre>
    <h3>Error logs:</h3>
    <pre class="logs">No error logs</pre>
  </body>
</html>""".trimIndent()
    }

    class Configuration(private val dir: File) : VerdictCases.Configuration {

        override fun wrongProjectDependencyFails() = """
<html>
  <head>
    <title>Build failed</title>
  </head>
  <body>
    <h2>FAILURE: Build failed with an exception</h2>
    <h3>What went wrong:</h3>
    <pre>Build file '${dir.canonicalPath}/app/build.gradle' line: 9
A problem occurred evaluating project ':app'.
	&gt; A problem occurred evaluating project ':app'.
		&gt; Project with path ':not-existed' could not be found in project ':app'.
</pre>
  </body>
</html>""".trimIndent()

        override fun illegalMethodFails() = """
<html>
  <head>
    <title>Build failed</title>
  </head>
  <body>
    <h2>FAILURE: Build completed with 2 failures.</h2>
    <h3>1: Task failed with an exception</h3>
    <pre>Build file '${dir.canonicalPath}/app/build.gradle' line: 7
A problem occurred evaluating project ':app'.
	&gt; A problem occurred evaluating project ':app'.
		&gt; Could not find method illegal() for arguments [build 'test-project'] on project ':app' of type org.gradle.api.Project.</pre>
""".trimIndent()
    }
}
