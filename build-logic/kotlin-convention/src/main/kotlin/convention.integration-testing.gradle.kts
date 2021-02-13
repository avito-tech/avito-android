import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("convention.libraries")
    id("nebula.integtest")
}

configure<KotlinJvmProjectExtension> {

    /**
     * Workaround to access internal classes from testFixtures
     * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
     */
    target.compilations.getByName("integTest").associateWith(target.compilations.getByName("main"))
}

plugins.withType<JavaTestFixturesPlugin>() {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("integTest")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
