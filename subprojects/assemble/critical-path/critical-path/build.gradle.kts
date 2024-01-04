plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:assemble:critical-path:api"))
    implementation(libs.gson)
    implementation(project(":subprojects:gradle:gradle-extensions"))

    gradleTestImplementation(project(":subprojects:common:junit-utils"))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
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
