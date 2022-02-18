plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    application
}

dependencies {
    implementation(libs.kotlinXCli)
    implementation(projects.subprojects.emcee.queueWorkerApi)
    implementation(projects.subprojects.emcee.androidDevice)
    implementation(libs.coroutinesCore)
}

application {
    mainClass.set("com.avito.emcee.worker.WorkerMain")
}
