plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.okhttp)
    implementation(Dependencies.funktionaleTry)

    implementation(project(":gradle:logging"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:git"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:instrumentation-tests-test-fixtures"))
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
