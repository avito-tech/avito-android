plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

dependencies {
    implementation(project(":cicd"))
    implementation(project(":impact"))
    implementation(project(":module-type"))
    implementation(project(":pre-build"))
    implementation(project(":utils"))
    implementation(project(":kotlin-dsl-support"))

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("codeOwnershipPlugin") {
            id = "com.avito.android.code-ownership"
            implementationClass = "com.avito.android.CodeOwnershipPlugin"
        }
    }
}
