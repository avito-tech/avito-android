package com.avito.report.model

import com.avito.android.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

internal class RunIdTest {

    @Test
    fun `serialize - without prefix`() {
        val actual = RunId(commitHash = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID).toString()
        val expected = "$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `serialize - with prefix`() {
        val actual = RunId(prefix = PREFIX, commitHash = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID).toString()
        val expected = "$PREFIX${RunId.DELIMITER}$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `serialize - blank prefix`() {
        val actual = RunId(prefix = "   ", commitHash = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID).toString()
        val expected = "$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `deserialize - without prefix`() {
        val actual = RunId.fromString("$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID")
        val expected = Result.Success(RunId(commitHash = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID))
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `deserialize - with prefix`() {
        val actual = RunId.fromString("$PREFIX${RunId.DELIMITER}$COMMIT_HASH${RunId.DELIMITER}$BUILD_TYPE_ID")
        val expected = Result.Success(RunId(prefix = PREFIX, commitHash = COMMIT_HASH, buildTypeId = BUILD_TYPE_ID))
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `deserialize - invalid parameter`() {
        val actual = RunId.fromString("1234567890abc")
        assertThat(actual).isInstanceOf(Result.Failure::class.java)
        assertThat((actual as Result.Failure).throwable).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `parameters could not contain delimiter`() {
        assertThrows(IllegalArgumentException::class.java) {
            RunId(RunId.DELIMITER.toString(), COMMIT_HASH, BUILD_TYPE_ID)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RunId(PREFIX, RunId.DELIMITER.toString(), BUILD_TYPE_ID)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RunId(PREFIX, COMMIT_HASH, RunId.DELIMITER.toString())
        }
    }

    private companion object {
        const val PREFIX = "prefix"
        const val COMMIT_HASH = "8982d2d2"
        const val BUILD_TYPE_ID = "local"
    }
}
