package com.avito.android.test.internal

import android.content.Context
import org.hamcrest.Matchers.`is`
import org.junit.Assert

internal class SQLiteDB(private val appContext: Context) {

    /**
     * Only works if all database connections are closed.
     * Does not produce error if connection still open.
     */
    fun clearAll() {
        appContext.databaseList().forEach { database ->
            val databasePath = appContext.getDatabasePath(database)
            appContext.deleteDatabase(database)

            val exists = databasePath.exists()
            Assert.assertThat("db exist not anymore at $databasePath", exists, `is`(false))
        }
    }
}
