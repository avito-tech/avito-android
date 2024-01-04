plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.ksp")
}

dependencies {
    api(project(":subprojects:gradle:module-types"))

    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:common:problem"))
    implementation(libs.kotlinGradle)

    implementation(libs.moshi)
    ksp(libs.moshiCodegen)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("validationsModuleTypes") {
            id = "com.avito.android.module-types-validator"
            implementationClass =
                "com.avito.android.module_type.validation.ModuleTypeValidationPlugin"
            displayName = "Module types validations"
        }
    }
}
