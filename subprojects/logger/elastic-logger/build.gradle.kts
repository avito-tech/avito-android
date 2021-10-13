plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.logger.logger)
    api(projects.subprojects.common.elastic)
}
