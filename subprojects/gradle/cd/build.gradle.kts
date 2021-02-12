plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-gradle-plugin")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.funktionaleTry)
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:artifactory-app-backup"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:instrumentation-tests"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:lint-report"))
    implementation(project(":subprojects:gradle:prosector"))
    implementation(project(":subprojects:gradle:qapps"))
    implementation(project(":subprojects:gradle:signer"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:test-summary"))
    implementation(project(":subprojects:gradle:tms"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(project(":subprojects:gradle:upload-to-googleplay"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(project(":subprojects:gradle:artifactory-app-backup-test-fixtures"))
    testImplementation(project(":subprojects:gradle:impact-shared-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd" // todo rename to ci-steps
            implementationClass = "com.avito.ci.CiStepsPlugin"
            displayName = "CI/CD"
        }
    }
}
