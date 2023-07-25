plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.gradle.moduleDependencies)

    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.problem)
    implementation(libs.kotlinGradle)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.module_type.ModuleTypesPlugin"
            displayName = "Module types"
        }

        create("validationsModuleTypesPlugin") {
            id = "com.avito.android.module-types-validator"
            implementationClass =
                "com.avito.android.module_type.validation.ModuleTypeValidationPlugin"
            displayName = "Module types validations"
        }
    }
}
