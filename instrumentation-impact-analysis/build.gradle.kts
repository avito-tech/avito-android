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
    implementation(project(":android"))
    implementation(project(":impact"))
    implementation(project(":impact-plugin"))
    implementation(project(":teamcity"))
    implementation(project(":git"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":files"))
    implementation(project(":sentry"))
    implementation(project(":statsd"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":ui-test-bytecode-analyzer"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinHtmlVersion")
    implementation("org.apache.bcel:bcel:$bcelVersion")

    testImplementation(project(":test-project"))
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
