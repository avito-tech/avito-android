plugins {
    application
    id("convention.kotlin-jvm")
    id("com.google.cloud.tools.jib") version "3.1.4"
    kotlin("plugin.serialization").version("1.6.21")
}

application {
    mainClass.set("com.avito.emcee.server.fake.MainKt")
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
