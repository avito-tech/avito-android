plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:common:okhttp"))
    api(libs.okhttpTls)
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:common:problem"))
    implementation(libs.kotlinGradle)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
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
