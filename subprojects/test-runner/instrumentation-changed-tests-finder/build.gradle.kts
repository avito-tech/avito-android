plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":common:kotlin-ast-parser")) {
        because("Need to know all classes in files")
    }
    implementation(project(":common:files"))
    implementation(project(":gradle:process"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":test-runner:instrumentation-tests-dex-loader")) {
        because("TestInApkModel")
    }
    implementation(project(":gradle:impact-shared")) {
        because("ChangesDetector reuse")
    }

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":gradle:git"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
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
