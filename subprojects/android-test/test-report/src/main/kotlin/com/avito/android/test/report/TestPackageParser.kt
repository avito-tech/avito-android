package com.avito.android.test.report

interface TestPackageParser {

    /**
     * null валидное значение для default package
     */
    fun parse(packageName: String?): Result

    sealed class Result {
        class Error(val error: Throwable) : Result()
        class Success(val features: List<String>) : Result()
    }

    class Impl : TestPackageParser {

        //todo не учитывает domofond
        private val prefix = "com.avito.android.test."

        override fun parse(packageName: String?): Result = try {
            val foundGroups = packageName
                ?.substringAfter(prefix)
                ?.split('.') ?: throw RuntimeException("Failed to find groups")

            val features = if (foundGroups.size > 1) {
                foundGroups.drop(1)
            } else {
                emptyList()
            }

            Result.Success(features)
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }
}
