plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinHtmlVersion: String by project
val bcelVersion: String by project
val gsonVersion: String by project
val kotsonVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:impact-plugin"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:ui-test-bytecode-analyzer"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinHtmlVersion")
    implementation("org.apache.bcel:bcel:$bcelVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation("com.google.code.gson:gson:$gsonVersion")
    testImplementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
}

gradlePlugin {
    plugins {
        create("instrumentationImpactAnalysis") {
            id = "com.avito.android.instrumentation-test-impact-analysis"
            implementationClass = "com.avito.instrumentation.impact.InstrumentationTestImpactAnalysisPlugin"
        }
    }
}
