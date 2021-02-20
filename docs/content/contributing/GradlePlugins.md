# Gradle plugins

## How to start

Start with official documentation:

- [Gradle plugin development tutorials](https://gradle.org/guides/?q=Plugin%20Development)
- [Custom tasks](https://docs.gradle.org/current/userguide/custom_tasks.html)

Gradle team's slack:

- [gradle-community.slack.com](https://gradle-community.slack.com)

## Working in IDE

### Known issues

- (DynamicTest.displayName) displays incorrectly in IDE: [#5975](https://github.com/gradle/gradle/issues/5975)

### IntelliJ IDEA

Preferred, but could not work if current Android Gradle Plugin is not supported yet

**Settings > Build, Execution, Deployment > Build Tools > Gradle > Runner**

- Delegate IDE build/run actions to Gradle (check)
- Run tests using : Gradle Test Runner

### Android Studio

2020.3.+ required to work with gradle integration tests in IDE, because intellij settings removed and gradle delegate
used under hood started with this release

## Testing Gradle plugins

### Isolating business-logic for unit-tests

You can isolate most of the logic from Gradle. Thus, it can be covered easily by unit-tests.

```kotlin
abstract class FeatureTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @TaskAction
    fun action() {
        val apiConfig = ... // get from the project
        workerExecutor.noIsolation().submit(FeatureWorkerAction::class.java) { parameters ->
            parameters.getIntegrationApiConfig().set(apiConfig)
        }
    }
}

// This wrapper is needed only for Worker API
// It can be started in another process. Thus, it has to prepare dependencies for the real work.

abstract class FeatureWorkerAction : WorkAction<Parameters> {

    interface Parameters : WorkParameters {
        fun getIntegrationApiConfig(): Property<IntegrationApiConfig>
    }

    override fun execute() {
        val api = IntegrationApiConfig.Impl(parameters.getIntegrationApiConfig().get())
        val action = FeatureAction(
            integrationApi = api
        )
        action.execute()
    }
}

// This class is responsible for the real work.
// The less it knows about Gradle, the better.

class FeatureAction(
    private val integrationApi: IntegrationApi
) {
    fun execute() {
        // Do the real work here
    }
}

// Now you can use simple mocks to test the action.
@Test
fun test() {
    val integrationApi = mock<IntegrationApi>()
    whenever(integrationApi.foo).thenReturn(bar())

    val action = FeatureAction(integrationApi) < --No Gradle abstractions here
        action.execute()

    assertThat(...)
}
```

### Integration tests

Apply a convention plugin:

```kotlin
plugins {
    id("convention.gradle-testing")
}
```

Place tests in `src/gradleTest/kotlin`

For simple cases you can create dummy instance of Project
by [ProjectBuilder](https://docs.gradle.org/current/javadoc/org/gradle/testfixtures/ProjectBuilder.html)

```kotlin
val project = ProjectBuilder.builder().build()

val task = project.tasks.register<TestTask>("testTask") {}

task.get().doStuff()
```

When you need to run a real build, use [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html).\
See ready utilities in `:test-project` module.

#### Debugging

## Debugging

In `GradleTestKit.kt` `fun gradlew()` set `withDebug(true)` to be able to debug gradle plugins. 

Disabled by default, because breaks tests even without debugging when android gradle plugin applied.

See [Gradle issue tracker](https://github.com/gradle/gradle/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+withDebug) about reasons.

### Run tests via CLI

`./gradlew test` - runs unit tests
`./gradlew gradleTest` - runs gradle integration tests

Add `--continue` if you don't need default fail fast on first failure

To run single test, package or class add `--tests package.class.method`, but keep in mind that it works only for a
single project

## Best practices

### Feature toggles

The plugin may break and blocks the work of other developers. Making the plugin unpluggable gives you time for a fix.

```kotlin
open class MyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // In each property use prefix `avito.<plugin>`
        // It makes it easy to find it in the future
        if (!project.getBooleanProperty("avito.my_plugin.enabled", default = false)) {
            project.logger.lifecycle("My plugin is disabled")
            return
        }
    }
}
```

## [Logging](../ci/Logging.md)
