plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:artifactory-app-backup"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:files"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:instrumentation-tests"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:lint-report"))
    implementation(project(":gradle:prosector"))
    implementation(project(":gradle:qapps"))
    implementation(project(":gradle:signer"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:test-summary"))
    implementation(project(":gradle:tms"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:upload-to-googleplay"))

    implementation(libs.kotlinStdlib)

    gradleTestImplementation(project(":common:test-okhttp"))
    gradleTestImplementation(project(":gradle:artifactory-app-backup-test-fixtures"))
    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(testFixtures(project(":common:logger")))
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
