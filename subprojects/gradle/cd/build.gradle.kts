plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.funktionaleTry)
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:signer"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:lint-report"))
    implementation(project(":subprojects:gradle:instrumentation-tests"))
    implementation(project(":subprojects:gradle:artifactory-app-backup"))
    implementation(project(":subprojects:gradle:performance"))
    implementation(project(":subprojects:gradle:qapps"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:prosector"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(project(":subprojects:gradle:upload-to-googleplay"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:gradle:artifactory-app-backup")))
}

configurations.all {
    resolutionStrategy {
        //docker client тянет 4.x версию
        force(Dependencies.okhttp)
    }
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd"
            implementationClass = "com.avito.ci.CdPlugin"
            displayName = "CI/CD"
        }
    }
}
