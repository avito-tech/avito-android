plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.codeOwnership.extensions)

    implementation(projects.subprojects.common.result)
    implementation(libs.androidToolsCommon)
    implementation(libs.androidGradle)
    gradleTestImplementation(libs.xmlUnit.core)
    gradleTestImplementation(libs.xmlUnit.matchers)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("deeplinkGeneratorPlugin") {
            id = "com.avito.android.deeplink-generator"
            implementationClass = "com.avito.deeplink_generator.DeeplinkGeneratorPlugin"
            displayName = "Automatic generation of AndroidManifest entries for public deeplinks"
        }

        // We need to include `subProjects.gradle.codeOwnership.plugin` in `subProjects.gradle.codeOwnership.plugin` for testing,
        // but gradle doesn't support add of implementation dependency
        // which produces several artifacts (codeOwnershipPlugin and codeOwnershipValidationPlugin).
        // Thus, we need to remove codeOwnershipValidationPlugin from subProjects.gradle.codeOwnership.plugin module.
        // We can't remove it completely because it will break backwards compatibility, so it temporally placed here.
        create("codeOwnershipValidationPlugin") { // TODO Remove after MBSA-974
            id = "com.avito.android.code-ownership-validation"
            implementationClass = "com.avito.android.CodeOwnershipValidationPlugin"
            displayName = "Ownership Validation"
        }
    }
}
