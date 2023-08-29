plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.ksp")
}

dependencies {
    api(projects.subprojects.gradle.moduleTypes)

    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.problem)
    implementation(libs.kotlinGradle)

    implementation(libs.moshi)
    ksp(libs.moshiCodegen)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
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
