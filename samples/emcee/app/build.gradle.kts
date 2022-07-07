import com.avito.kotlin.dsl.getMandatoryStringProperty
import java.time.Duration

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.avito.android.emcee")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.avito.emcee"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

emcee {
    queueBaseUrl.set("http://localhost:41000")
    testTimeout.set(Duration.ofSeconds(1))
    artifactory {
        // put properties to .gradle/gradle.properties because say contains sensitive data
        user.set(project.getMandatoryStringProperty("emcee.sample.artifactory.user"))
        password.set(project.getMandatoryStringProperty("emcee.sample.artifactory.password"))
        baseUrl.set(project.getMandatoryStringProperty("emcee.sample.artifactory.url"))
        repository.set(project.getMandatoryStringProperty("emcee.sample.artifactory.repository"))
    }
    job {
        groupId.set("Emcee Android Sample")
        groupPriority.set(1)
        id.set("Generate")
        priority.set(1)
    }
    retries.set(1)
    outputDir.set(project.layout.buildDirectory.dir("emcee"))
    deviceApis.set(listOf(21))
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
}
