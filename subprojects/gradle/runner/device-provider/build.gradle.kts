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
    api(project(":subprojects:gradle:runner:service"))
    api(project(":subprojects:gradle:runner:shared"))
    api(project(":subprojects:gradle:runner:stub"))
    api(project(":subprojects:gradle:kubernetes"))

    implementation(Dependencies.funktionaleTry)
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:common:logger"))

    testImplementation(project(":subprojects:common:logger-test-fixtures"))
}

kotlin {
    explicitApi()

    /**
     * Workaround to access internal classes from testFixtures
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
