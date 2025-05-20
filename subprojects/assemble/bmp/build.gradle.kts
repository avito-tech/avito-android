plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    compileOnly(gradleApi())
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("bmp") {
            id = "com.avito.android.bmp"
            implementationClass = "com.avito.android.bmp.BuildMetricsPlugin"
            displayName = "Build metrics plugin"
        }
    }
}
