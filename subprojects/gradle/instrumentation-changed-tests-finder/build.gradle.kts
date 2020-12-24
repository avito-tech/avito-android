plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
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

    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":gradle:git"))
    testImplementation(project(":gradle:impact-shared-test-fixtures"))
    testImplementation(project(":gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":gradle:test-project")) {
        because("File extensions") // todo probably move to :common:files
    }
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
