plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.ksp")
}

dependencies {
    api(libs.moshi)
    api(projects.subprojects.gradle.codeOwnership.extensions)

    implementation(libs.jacksonDataformat.toml)
    implementation(projects.subprojects.gradle.preBuild)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.okhttp)

    ksp(libs.moshiCodegen)
    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
