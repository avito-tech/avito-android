import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("nebula.integtest") version "9.5.2"
    application
}

repositories {
    mavenCentral()
}

// Implementation shouldn't depend on any other module in the repository to avoid chained changes!
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")

    integTestImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
}

application {
    mainClass.set("ru.avito.image_builder.Main")
}

kotlin {
    explicitApi()
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
