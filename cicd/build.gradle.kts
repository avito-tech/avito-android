plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val okhttpVersion: String by project
val funktionaleVersion: String by project

dependencies {
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation(project(":android"))
    implementation(project(":impact"))
    implementation(project(":impact-plugin"))
    implementation(project(":docs-deployer"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":signer"))
    implementation(project(":files"))
    implementation(project(":lint-report"))
    implementation(project(":instrumentation"))
    implementation(project(":artifactory"))
    implementation(project(":performance"))
    implementation(project(":qapps"))
    implementation(project(":teamcity"))
    implementation(project(":prosector"))
    implementation(project(":git"))
    implementation(project(":upload-cd-build-result"))
    implementation(project(":upload-to-googleplay"))

    testImplementation(project(":test-project"))
    testImplementation(project(":test-okhttp"))
    testImplementation(testFixtures(project(":artifactory")))
}

configurations.all {
    resolutionStrategy {
        //docker client тянет 4.x версию
        force("com.squareup.okhttp3:okhttp:$okhttpVersion")
    }
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd"
            implementationClass = "com.avito.ci.CdPlugin"
        }
    }
}
