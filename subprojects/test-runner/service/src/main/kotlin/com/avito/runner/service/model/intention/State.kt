package com.avito.runner.service.model.intention

public data class State(
    val layers: List<Layer>,
    val digest: String = layers.hashCode().toString() // layers order matter
) {

    public sealed class Layer {

        public data class ApiLevel(val api: Int) : Layer() {

            override fun toString(): String = "Api level = $api"
        }

        public data class Model(val model: String) : Layer() {

            override fun toString(): String = "Model = $model"
        }

        public data class InstalledApplication(
            val applicationPath: String,
            val applicationPackage: String
        ) : Layer() {

            override fun toString(): String =
                "Application: $applicationPath, package: $applicationPackage"

            internal companion object
        }
    }

    override fun toString(): String = buildString {
        append("State=$digest with layers: {")
        append(layers.joinToString(prefix = "[Layer: ", postfix = "]"))
        append("}")
    }
}
