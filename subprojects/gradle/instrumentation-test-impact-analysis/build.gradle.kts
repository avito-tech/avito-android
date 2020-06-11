plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:android"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:impact"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:ui-test-bytecode-analyzer"))
    implementation(project(":gradle:worker"))
    implementation(Dependencies.kotlinCompilerEmbeddable)
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)

    testImplementation(project(":gradle:test-project"))
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
