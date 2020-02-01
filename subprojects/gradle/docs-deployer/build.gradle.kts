plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val funktionaleVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:docker"))
    implementation(project(":subprojects:gradle:kubernetes"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

gradlePlugin {
    plugins {
        create("docsDeployPlugin") {
            id = "com.avito.android.docs"
            implementationClass = "com.avito.android.plugin.DocsPlugin"
        }
    }
}
