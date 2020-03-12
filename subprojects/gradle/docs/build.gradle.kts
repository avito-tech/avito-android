plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:docker"))
    implementation(project(":subprojects:gradle:kubernetes"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(Dependencies.funktionaleTry)
}

gradlePlugin {
    plugins {
        create("docsDeployPlugin") {
            id = "com.avito.android.docs"
            implementationClass = "com.avito.android.plugin.DocsPlugin"
            displayName = "Docs deployer"
        }
    }
}
