plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.unit-testing")
    id("convention.ksp")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:module-types"))

    implementation(libs.moshiAdapters)
    implementation(libs.dependencyAnalysis)

    testImplementation(libs.junit)

    ksp(libs.moshiCodegen)
}

gradlePlugin {
    plugins {
        create("moduleApiExtraction") {
            id = "com.avito.android.module-api-extraction"
            implementationClass = "com.avito.module_api_extraction.ModuleApiExtractionPlugin"
            displayName = "Module Api Extraction"
        }
    }
}
