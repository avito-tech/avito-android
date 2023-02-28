plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.ksp")
}

dependencies {
    implementation(libs.kotlinGradle)
    implementation(libs.moshi)
    implementation(libs.moshiRetrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.codeOwnership.plugin)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.common.compositeException)

    ksp(libs.moshiCodegen)

    testImplementation(testFixtures(projects.subprojects.gradle.codeOwnership.plugin))
    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(testFixtures(projects.subprojects.gradle.codeOwnership.plugin))
    gradleTestImplementation(libs.truth)
}

gradlePlugin {
    plugins {
        create("techBudget") {
            id = "com.avito.android.tech-budget"
            implementationClass = "com.avito.android.tech_budget.TechBudgetPlugin"
            displayName = "Tech budget"
        }
    }
}
