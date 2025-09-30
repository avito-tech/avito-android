plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.dependencyAnalysis)
}

gradlePlugin {
    plugins {
        create("dependencyAnalysis") {
            id = "com.avito.android.dependency-analysis"
            implementationClass = "com.avito.android.dependency_analysis.CustomDependencyAnalysisPlugin"
            displayName = "Custom dependency analysis"
        }
    }
}
