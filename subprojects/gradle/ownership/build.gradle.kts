plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:cicd"))
    implementation(project(":subprojects:gradle:impact"))
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
        }
    }
}
