package com.avito.android.network_contracts.scheme.output.parsers

import com.avito.android.network_contracts.output.parsers.CodegenJsonOutputTransformer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CodegenJsonOutputTransformerTest {

    @Test
    fun `valid json - return list of errors`() {
        val output = """
            {"msg":"client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"validation","SCOPE":"validation"}
            {"msg":"client: схема клиента /app/route2/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"generation","SCOPE":"generation"}
        """.trimIndent()
        val transformer = CodegenJsonOutputTransformer()
        val actual = transformer.transform(output)
        val expect = """
            Validation failures:
             - client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса
            
            Generation failures:
             - client: схема клиента /app/route2/path_schema.yaml не валидна относительно схемы сервиса
            
        """.trimIndent()
        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `same errors - return unique list of errors`() {
        val output = """
            {"msg":"client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"validation","SCOPE":"validation"}
            {"msg":"client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"validation","SCOPE":"validation"}
            {"msg":"client: схема клиента /app/route2/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"validation","SCOPE":"validation"}
        """.trimIndent()
        val transformer = CodegenJsonOutputTransformer()
        val actual = transformer.transform(output)
        val expect = """
            Validation failures:
             - client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса
             - client: схема клиента /app/route2/path_schema.yaml не валидна относительно схемы сервиса
            
        """.trimIndent()
        assertThat(actual).isEqualTo(expect)
    }

    @Test
    fun `invalid json - return output as is`() {
        val output = """
            {,"msg":"client: схема клиента /app/route/path_schema.yaml не валидна относительно схемы сервиса","TITLE":"validation","SCOPE":"validation"}
        """.trimIndent()
        val transformer = CodegenJsonOutputTransformer()
        val actual = transformer.transform(output)
        assertThat(actual).isEqualTo(output)
    }
}
