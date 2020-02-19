package com.avito.runner.service.model.intention

data class State(
    val layers: List<Layer>,
    val digest: String = layers.hashCode().toString() //layers order matter
) {

    sealed class Layer {
        data class ApiLevel(val api: Int) : Layer() {
            override fun toString(): String = "Api level = $api"
        }

        data class Model(val model: String) : Layer() {
            override fun toString(): String = "Model = $model"
        }

        data class InstalledApplication(
            val applicationPath: String,
            val applicationPackage: String
        ) : Layer() {
            override fun toString(): String =
                "Application: $applicationPath with package: $applicationPackage installed"
        }
    }

    override fun toString(): String = buildString {
        append("State ($digest) with layers:")
        layers.forEach { append(" $it") }
    }
}
