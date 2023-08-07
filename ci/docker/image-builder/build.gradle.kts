import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nebula)
    application
}

repositories {
    mavenCentral()
}

// Implementation shouldn't depend on any other module in the repository to avoid chained changes!
dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlinx.cli)

    testImplementation(libs.junit.jupiter)
    integTestImplementation(libs.junit.jupiter)
}

application {
    mainClass.set("ru.avito.image_builder.Main")
}

kotlin {
    explicitApi()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks {
    val fatJarTask = register<Jar>("fatJar") {
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output

        from(contents)
        dependsOn("assemble")
    }
    build {
        dependsOn(fatJarTask)
    }
}

configure<KotlinJvmProjectExtension> {
    target.compilations.getByName("integTest")
        .associateWith(target.compilations.getByName("main"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
