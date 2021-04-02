plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(gradleApi())
    implementation(project(":common:throwable-utils"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(libs.kotlinStdlib)
    implementation(libs.gson)
    implementation(libs.kotlinHtml)

    gradleTestImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildVerdict") {
            id = "com.avito.android.build-verdict"
            implementationClass = "com.avito.android.build_verdict.BuildVerdictPlugin"
            displayName = "Create file with a build verdict"
        }
    }
}
