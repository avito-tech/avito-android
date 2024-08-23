plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:test-model"))

    implementation(libs.commonsText) {
        because("for StringEscapeUtils.escapeXml10() only")
    }
}
