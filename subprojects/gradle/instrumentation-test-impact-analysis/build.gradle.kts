plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:percent"))
    implementation(project(":common:kotlin-ast-parser"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:files"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:ui-test-bytecode-analyzer"))
    implementation(project(":gradle:worker"))
    implementation(Dependencies.kotlinCompilerEmbeddable)
    implementation(Dependencies.Gradle.kotlinPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:logger-test-fixtures"))
}

gradlePlugin {
    plugins {
        create("instrumentationImpactAnalysis") {
            id = "com.avito.android.instrumentation-test-impact-analysis"
            implementationClass = "com.avito.instrumentation.impact.InstrumentationTestImpactAnalysisPlugin"
            displayName = "Instrumentation tests impact analysis"
        }
    }
}
