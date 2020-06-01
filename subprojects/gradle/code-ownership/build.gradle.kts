plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:cd"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:module-types"))
    implementation(project(":gradle:pre-build"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:kotlin-dsl-support"))

    testImplementation(project(":gradle:test-project"))
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
