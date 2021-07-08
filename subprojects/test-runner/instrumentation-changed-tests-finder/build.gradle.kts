plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.common.kotlinAstParser) {
        because("Need to know all classes in files")
    }
    implementation(projects.common.files)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.impactShared) {
        because("ChangesDetector reuse")
    }

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.gradle.git)
    gradleTestImplementation(testFixtures(projects.logger.logger))
}

gradlePlugin {
    plugins {
        create("instrumentationModifiedTestsFinder") {
            id = "com.avito.android.instrumentation-changed-tests-finder"
            implementationClass = "com.avito.android.ChangedTestsFinderPlugin"
            displayName = "Instrumentation changed tests finder"
        }
    }
}
