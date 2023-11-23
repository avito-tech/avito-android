plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.okhttp)
    api(libs.okhttpTls)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.problem)
    implementation(libs.kotlinGradle)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("tlsConfiguration") {
            id = "com.avito.android.tls-configuration"
            implementationClass =
                "com.avito.android.tls.TlsConfigurationPlugin"
            displayName = "mTls configuration"
        }
    }
}
