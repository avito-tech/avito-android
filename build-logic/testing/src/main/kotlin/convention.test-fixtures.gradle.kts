import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    `java-test-fixtures`
}

configure<KotlinJvmProjectExtension> {

    /**
     * Workaround to access internal classes from testFixtures
     * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
     */
    target.compilations.getByName("testFixtures")
        .associateWith(target.compilations.getByName("main"))
}

(components["java"] as AdhocComponentWithVariants).skipTestFixturesPublication()

/**
 * from: https://docs.gradle.org/current/userguide/java_testing.html#publishing_test_fixtures
 */
fun AdhocComponentWithVariants.skipTestFixturesPublication() {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}
