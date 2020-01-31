plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":impact"))
    implementation(project(":utils"))
    implementation(project(":pre-build"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":impact-plugin"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.ModuleTypesPlugin"
        }
    }
}
