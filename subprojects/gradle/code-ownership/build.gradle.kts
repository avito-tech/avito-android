plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin-legacy")
    id("convention.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:cd"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:module-types"))
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
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
