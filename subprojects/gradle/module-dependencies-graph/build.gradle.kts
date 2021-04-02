plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.libraries")
}

dependencies {
    compileOnly(gradleApi())

    implementation(projects.gradle.gradleLogger)
    implementation(projects.common.math)

    gradleTestImplementation(projects.common.truthExtensions)
    gradleTestImplementation(testFixtures(projects.common.logger))
    gradleTestImplementation(projects.gradle.testProject)
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
