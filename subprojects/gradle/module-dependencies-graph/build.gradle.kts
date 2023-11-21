plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.subprojects.gradle.moduleDependencies)
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(libs.jdgraphtCore)
    implementation(libs.jacksonDataformat.csv)

    gradleTestImplementation(projects.subprojects.common.truthExtensions)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
