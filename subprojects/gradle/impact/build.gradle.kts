plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(
        project(":gradle:impact-shared")
    )
    implementation(gradleApi())
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(Dependencies.antPattern)
    implementation(Dependencies.gradle.kotlinPlugin)

    testImplementation(project(":gradle:impact-shared-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:logging-test-fixtures"))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(Dependencies.test.mockitoKotlin)
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
