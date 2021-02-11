plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:gradle:build-verdict-tasks-api"))
    implementation(gradleApi())
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(Dependencies.gson)
    implementation(Dependencies.kotlinHtml)
    testImplementation(project(":subprojects:gradle:test-project"))
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
