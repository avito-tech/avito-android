plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.ksp")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.kotlinGradle)
    implementation(libs.moshi)
    implementation(libs.moshiRetrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.detektGradle)
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:code-ownership:plugin"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:module-dependencies-graph"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:tech-budget-common"))
    implementation(project(":subprojects:gradle:module-types-api"))
    runtimeOnly(project(":subprojects:gradle:module-types")) {
        because("Need to run gradleTest. Adding to gradleTestRuntime doesn't work")
    }

    ksp(libs.moshiCodegen)

    testImplementation(testFixtures(project(":subprojects:gradle:code-ownership:plugin")))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
    gradleTestImplementation(testFixtures(project(":subprojects:gradle:code-ownership:plugin")))
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
