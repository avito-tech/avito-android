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
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:instrumentation-tests-dex-loader")) {
        because("TestInApkModel")
    }
    implementation(project(":gradle:impact-shared")) {
        because("ChangesDetector reuse")
    }
    implementation(libs.kotlinStdlib)

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":gradle:git"))
    gradleTestImplementation(testFixtures(project(":common:logger")))
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
