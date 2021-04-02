@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("nebula.integtest")
}

configure<KotlinJvmProjectExtension> {
    target.compilations.getByName("integTest")
        .associateWith(target.compilations.getByName("main"))
}

plugins.withType<JavaTestFixturesPlugin> {

    configure<KotlinJvmProjectExtension> {
        target.compilations.getByName("integTest")
            .associateWith(target.compilations.getByName("testFixtures"))
    }
}
