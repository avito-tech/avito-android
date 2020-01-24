plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":docker"))
    implementation(project(":sentry"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.fkorotkov:kubernetes-dsl:1.2.1")
    implementation("io.fabric8:kubernetes-client:4.1.3")
}

gradlePlugin {
    plugins {
        create("docsDeployPlugin") {
            id = "com.avito.android.docs"
            implementationClass = "com.avito.android.plugin.DocsPlugin"
        }
    }
}
