plugins {
    id("kotlin")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:cd"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:module-type"))
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

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
