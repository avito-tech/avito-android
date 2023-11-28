package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import java.util.Base64

internal val errorsSchema = generateSchemaResponseFormat(
    filePath = "/api_schema_1/error.yml",
    content = """
            components:
              schemas:
                ErrorBadRequest:
                  description: Нарушены требования протокола (напр. не передан обязательный параметр)
                  properties:
                    bad-request:
                      properties:
                        message:
                          description: Сообщение об ошибке
                          type: string
                      required:
                        - message
                      title: error_bad_request_bad_request
                      type: object
                  required:
                    - bad-request
                  title: error_bad_request
                  type: object
                ErrorUnauthorized:
                  description: Сессия не прошла проверку.
                  properties:
                    unauthorized:
                      properties:
                        message:
                          description: Сообщение об ошибке
                          type: string
                      required:
                        - message
                      title: error_unauthorized_unauthorized
                      type: object
                  required:
                    - unauthorized
                  title: error_unauthorized
                  type: object
            info:
              title: __components_file__
              version: 1.0.0
            openapi: 3.0.0
            paths: {}
    """.trimIndent()
)

internal val modelSchema = generateSchemaResponseFormat(
    filePath = "/api_schema_1/models/schema.yml",
    content = """
        components:
          schemas:
            Response:
              description: Данные
              properties:
                success:
                  nullable: true
                  properties:
                    image:
                      additionalProperties:
                        type: string
                      type: object
                  required:
                    - image
                  title: response_success
                  type: object
              title: response
              type: object
        info:
          title: __components_file__
          version: 1.0.0
        openapi: 3.0.0
        paths: {}
    """.trimIndent()
)

private fun generateSchemaResponseFormat(filePath: String, content: String): SchemaEntry {
    return SchemaEntry(
        path = filePath,
        content = Base64.getEncoder().encodeToString(content.toByteArray())
    )
}
