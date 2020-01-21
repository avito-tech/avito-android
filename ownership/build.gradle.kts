plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":cicd"))
    implementation(project(":impact"))
    implementation(project(":module-type"))
    implementation(project(":utils"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

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
