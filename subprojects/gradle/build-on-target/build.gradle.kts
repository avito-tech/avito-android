plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.okhttp)
    implementation(Dependencies.funktionaleTry)

    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-test-fixtures"))
}

gradlePlugin {
    plugins {
        create("buildOnTarget") {
            id = "com.avito.android.build-on-target"
            implementationClass = "com.avito.buildontarget.BuildOnTargetPlugin"
            displayName = "Build on target"
        }
    }
}
