plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:pre-build"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:impact"))
    implementation(libs.kotlinPlugin)
    implementation(libs.kotlinStdlib)

    gradleTestImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.ModuleTypesPlugin"
            displayName = "Module types"
        }
    }
}
