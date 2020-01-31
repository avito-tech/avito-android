plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:impact-plugin"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "ModuleTypesPlugin"
        }
    }
}
