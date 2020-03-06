plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:ui-test-bytecode-analyzer"))
    implementation(Dependencies.kotlinCompilerEmbeddable)
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)

    testImplementation(project(":subprojects:gradle:test-project"))
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
