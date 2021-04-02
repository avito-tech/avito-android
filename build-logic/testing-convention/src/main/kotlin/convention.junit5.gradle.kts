import gradle.kotlin.dsl.accessors._0fb62041f0aa089ab662fbbb185e1bcf.sourceSets
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

val junit5Version = "5.7.1"
val junit5PlatformVersion = "1.6.0"

val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:$junit5Version"
val junitPlatformRunner = "org.junit.platform:junit-platform-runner:$junit5PlatformVersion"
val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:$junit5PlatformVersion"
val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:$junit5Version"
val truth = "com.google.truth:truth:1.0"

/**
 * Can't use versions catalog in precompiled script plugins
 * https://github.com/gradle/gradle/issues/15383
 */
plugins.withType<KotlinBasePluginWrapper> {

    sourceSets.matching { it.name == "gradleTest" }.whenObjectAdded {
        dependencies {
            add("gradleTestImplementation", gradleTestKit())
            add("gradleTestImplementation", junitJupiterApi)
            add("gradleTestImplementation", truth)
            add("gradleTestRuntimeOnly", junitJupiterEngine)
            add("gradleTestRuntimeOnly", junitPlatformRunner)
            add("gradleTestRuntimeOnly", junitPlatformLauncher)
        }
    }

    dependencies {
        add("testImplementation", junitJupiterApi)
        add("testImplementation", truth)

        add("testRuntimeOnly", junitJupiterEngine)
        add("testRuntimeOnly", junitPlatformRunner)
        add("testRuntimeOnly", junitPlatformLauncher)
    }
}
