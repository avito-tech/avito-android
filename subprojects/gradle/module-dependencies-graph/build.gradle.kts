import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.configurationcache.extensions.serviceOf

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":logger:gradle-logger"))
    implementation(project(":common:math"))

    gradleTestImplementation(project(":common:truth-extensions"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
    gradleTestImplementation(project(":gradle:test-project"))

    // workaround for https://github.com/gradle/gradle/issues/16774
    gradleTestRuntimeOnly(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders").classpath.asFiles.first()
        )
    )
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("moduleDependenciesGraph") {
            id = "com.avito.android.module-dependencies-graph"
            implementationClass = "com.avito.module.dependencies.ModuleDependenciesGraphPlugin"
            displayName = "Module dependencies graph"
            description = "Build module dependencies graph"
        }
    }
}
