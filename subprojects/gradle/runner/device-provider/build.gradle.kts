plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    `java-test-fixtures`
    id("nebula.integtest")
}

extra["artifact-id"] = "runner-device-provider"

dependencies {
    // todo api impl?
    api(project(":gradle:runner:service"))
    api(project(":gradle:runner:shared"))
    api(project(":gradle:runner:stub"))
    api(project(":gradle:kubernetes"))

    implementation(Dependencies.funktionaleTry)
    implementation(project(":gradle:process"))
    implementation(project(":common:logger"))

    testImplementation(project(":common:logger-test-fixtures"))
}

kotlin {
    explicitApi()

    /**
     * Workaround to access internal classes from textFixtures
     * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
     */
    target.compilations
        .matching { it.name in listOf("testFixtures", "integTest") }
        .configureEach {
            associateWith(target.compilations.getByName("main"))
        }

    target.compilations
        .matching { it.name in listOf("integTest") }
        .configureEach {
            associateWith(target.compilations.getByName("testFixtures"))
        }
}
