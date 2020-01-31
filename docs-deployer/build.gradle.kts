plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val funktionaleVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":docker"))
    implementation(project(":kubernetes"))
    implementation(project(":sentry"))
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
