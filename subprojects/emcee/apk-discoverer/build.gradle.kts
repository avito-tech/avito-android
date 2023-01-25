plugins {
    id("convention.kotlin-jvm")
    application
}

dependencies {
    implementation(libs.dextestparser)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.bundles.ktor)
}

application {
    mainClass.set("com.avito.emcee.discoverer.EmceeApkDiscoverer")
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

        archiveFileName.set("emcee-apk-discoverer.jar")

        from(contents)
        dependsOn("assemble")
    }
    build {
        dependsOn(fatJarTask)
    }
}
