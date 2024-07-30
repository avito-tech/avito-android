plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:gradle:module-types-api"))
    api(project(":subprojects:gradle:module-dependencies"))

    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:common:problem"))
    implementation(libs.kotlinGradle)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.module_type.ModuleTypesPlugin"
            displayName = "Module types"
        }
    }
}
