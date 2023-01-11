plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
    application
}

dependencies {
    implementation(projects.subprojects.emcee.queueWorkerApi)
    implementation(projects.subprojects.emcee.androidDevice)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.common.problem)
    implementation(libs.kotlinXCli)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.moshi)
    implementation(libs.bundles.ktor)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    ksp(libs.moshiCodegen)

    testImplementation(libs.truth)
    testImplementation(libs.ktorClient)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
}

application {
    mainClass.set("com.avito.emcee.worker.WorkerMain")
}

tasks {
    val fatJarTask = register<Jar>("fatJar") {
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
        // TODO: use DuplicatesStrategy.FAIL instead
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output

        archiveFileName.set("emcee-worker.jar")

        from(contents)
        dependsOn("assemble")
    }
    build {
        dependsOn(fatJarTask)
    }
}
