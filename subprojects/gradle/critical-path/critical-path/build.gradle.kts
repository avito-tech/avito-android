plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":gradle:critical-path:api"))
    implementation(project(":logger:gradle-logger"))
    implementation(libs.gson)
    implementation(project(":gradle:gradle-extensions"))

    gradleTestImplementation(project(":common:junit-utils"))
    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("criticalPath") {
            id = "com.avito.android.critical-path"
            implementationClass = "com.avito.android.critical_path.CriticalPathPlugin"
            displayName = "Build critical path"
            description = "Calculates critical path of a build. These are tasks that define build duration."
        }
    }
}
