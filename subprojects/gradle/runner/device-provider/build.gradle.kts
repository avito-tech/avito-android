plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    `java-test-fixtures`
    id("nebula.integtest")
    id("convention.libraries")
}

extra["artifact-id"] = "runner-device-provider"

dependencies {
    // todo api impl?
    api(project(":subprojects:gradle:runner:service"))
    api(project(":subprojects:gradle:runner:shared"))
    api(project(":subprojects:gradle:runner:stub"))
    api(project(":subprojects:gradle:kubernetes"))

    implementation(libs.funktionaleTry)
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:common:logger"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
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
