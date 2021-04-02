plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:cd"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:module-types"))
    implementation(project(":gradle:pre-build"))
    implementation(project(":gradle:gradle-extensions"))

    implementation(libs.kotlinStdlib)

    gradleTestImplementation(project(":gradle:test-project"))
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
