import org.gradle.kotlin.dsl.`java-test-fixtures`
import org.gradle.kotlin.dsl.configure
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
