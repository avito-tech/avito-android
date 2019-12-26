plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.avito"
version = "1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = "5.6.4"
    }
}
