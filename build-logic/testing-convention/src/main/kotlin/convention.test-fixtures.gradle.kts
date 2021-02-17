import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("convention.libraries")
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

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
