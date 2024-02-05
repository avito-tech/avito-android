plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.ksp")
}

dependencies {
    api(libs.moshi)
    api(project(":subprojects:gradle:code-ownership:extensions"))

    implementation(libs.jacksonDataformat.toml)
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:module-dependencies-graph"))
    implementation(project(":subprojects:common:okhttp"))

    ksp(libs.moshiCodegen)
    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("codeOwnershipPlugin") {
            id = "com.avito.android.code-ownership"
            implementationClass = "com.avito.android.CodeOwnershipPlugin"
            displayName = "Ownership"
        }
    }
}
