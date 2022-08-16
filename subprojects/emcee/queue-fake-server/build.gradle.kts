import com.avito.kotlin.dsl.getMandatoryStringProperty

plugins {
    application
    id("convention.kotlin-jvm")
    id("com.bmuschko.docker-java-application") version "8.0.0"
    kotlin("plugin.serialization").version("1.6.21")
}

application {
    mainClass.set("com.avito.emcee.server.fake.MainKt")
}

val registry = project.getMandatoryStringProperty("avito.docker.registry", allowBlank = false)

docker {
    javaApplication {
        baseImage.set("$registry/android/openjdk:11")
        ports.set(setOf(41000))
        images.set(setOf("emcee-queue:latest"))
        mainClassName.set("com.avito.emcee.server.fake.MainKt")
    }
}

dependencies {
    implementation(projects.subprojects.emcee.queueClientApi)
    implementation("ch.qos.logback:logback-classic:1.2.11") {
        because("Enable ktor server logs")
    }
    implementation("io.ktor:ktor-server-core-jvm:2.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.3")
    implementation("io.ktor:ktor-serialization-gson:2.0.3")
}
