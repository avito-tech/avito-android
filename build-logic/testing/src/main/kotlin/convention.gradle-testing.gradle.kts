import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.configurationcache.extensions.serviceOf
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.utils.addExtendsFromRelation

plugins {
    id("kotlin")
    id("java-gradle-plugin")
    idea
}

val gradleTest: SourceSet by sourceSets.creating {
    addExtendsFromRelation(
        extendingConfigurationName = "gradleTestImplementation",
        extendsFromConfigurationName = "testImplementation"
    )

    addExtendsFromRelation(
        extendingConfigurationName = "gradleTestRuntimeOnly",
        extendsFromConfigurationName = "testRuntimeOnly"
    )
}

val gradleTestJarTask = tasks.register<Jar>(gradleTest.jarTaskName) {
    archiveClassifier.set("gradle-tests")
    from(gradleTest.output)
}

val testTimeoutSeconds = 600

val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl").forUseAtConfigurationTime()

val gradleTestTask = tasks.register<Test>("gradleTest") {
    description = "Runs gradle test kit tests"
    group = "verification"

    testClassesDirs = gradleTest.output.classesDirs
    classpath = configurations[gradleTest.runtimeClasspathConfigurationName] + files(gradleTestJarTask)

    /**
     * The only reason to have more forks is faster test suite because of parallel execution
     * Additional forks requires more resources and should be faster
     * Tests on powerful machine with a lot of resources to spare proves that actually 1 is the fastest value,
     * at least for our project
     *
     * `make benchmark_gradle_test` used for tests (see gradle/performance.scenarios)
     * forks median value:
     *   1    4min 11sec
     *   2    4min 39sec
     */
    maxParallelForks = 1

    /**
     * usually there is a small amount of test classes per module, and tests are not so memory hungry
     */
    setForkEvery(null)

    jvmArgs(
        listOf(
            "-XX:+UseGCOverheadLimit",
            "-XX:GCTimeLimit=10"
        )
    )

    minHeapSize = "128m"
    maxHeapSize = "256m"

    systemProperty("rootDir", "${project.rootDir}")
    systemProperty("buildDir", "$buildDir")
    systemProperty("kotlinVersion", project.getKotlinPluginVersion())

    systemProperty("artifactoryUrl", artifactoryUrl.getOrElse(""))
    systemProperty("isTest", true)

    systemProperty("junit.jupiter.execution.timeout.default", testTimeoutSeconds)
}

var compileSdkVersion: Int? = null
var buildToolsVersion: String? = null

// workaround for https://github.com/gradle/gradle/issues/15383
if (project.name != "gradle-kotlin-dsl-accessors") {
    val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
    compileSdkVersion = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
}

gradleTestTask.configure {
    compileSdkVersion?.let { systemProperty("compileSdkVersion", it) }
    buildToolsVersion?.let { systemProperty("buildToolsVersion", it) }
}

dependencies {
    // workaround for https://github.com/gradle/gradle/issues/16774
    "gradleTestRuntimeOnly"(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders").classpath.asFiles.first()
        )
    )
}

gradlePlugin {
    testSourceSets(gradleTest)
}

// make idea to treat gradleTest as test sources
idea {
    gradleTest.allSource.srcDirs.forEach { srcDir ->
        module.testSourceDirs = module.testSourceDirs + srcDir
    }

    module.scopes["TEST"]?.get("plus")?.plusAssign(
        listOf(
            configurations.getByName(gradleTest.compileClasspathConfigurationName),
            configurations.getByName(gradleTest.runtimeClasspathConfigurationName)
        )
    )
}

tasks.check.configure {
    dependsOn(gradleTestTask)
}

configure<KotlinJvmProjectExtension> {

    target.compilations.getByName("gradleTest")
        .associateWith(target.compilations.getByName("main"))

    plugins.withType<JavaTestFixturesPlugin> {
        target.compilations.getByName("gradleTest")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
