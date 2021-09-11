plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    compileOnly(gradleApi())

    implementation(projects.logger.gradleLogger)
    implementation(projects.common.math)
    implementation(projects.gradle.gradleExtensions)

    gradleTestImplementation(projects.common.truthExtensions)
    gradleTestImplementation(testFixtures(projects.logger.logger))
    gradleTestImplementation(projects.gradle.testProject)
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
