package com.lutukai.simpletodoapp.data.local.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests for AppDataBase.
 *
 * Currently the database is at version 1, so there are no migrations to test.
 * This file provides the infrastructure for testing future migrations.
 *
 * When you add a migration (e.g., version 1 to 2), add a test like:
 *
 * ```kotlin
 * @Test
 * fun migrate1To2() {
 *     // Create version 1 database
 *     helper.createDatabase(TEST_DB, 1).apply {
 *         execSQL("INSERT INTO todos (title, description, isCompleted, completedAt, createdAt) VALUES ('Test', '', 0, NULL, 123456)")
 *         close()
 *     }
 *
 *     // Run migration and validate
 *     helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
 *
 *     // Verify data was migrated correctly
 *     val db = getMigratedRoomDatabase()
 *     val todos = db.todoDao().getAllTodos().blockingFirst()
 *     assertThat(todos).hasSize(1)
 *     assertThat(todos[0].title).isEqualTo("Test")
 * }
 * ```
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDataBase::class.java
    )

    @Test
    fun createDatabaseVersion1_schemaIsValid() {
        // Create database at version 1 and verify it can be created
        helper.createDatabase(TEST_DB, 1).apply {
            // Verify the todos table exists with expected columns
            val cursor = query("SELECT * FROM todos LIMIT 0")
            val columnNames = cursor.columnNames.toList()

            assertThat(columnNames).contains("id")
            assertThat(columnNames).contains("title")
            assertThat(columnNames).contains("description")
            assertThat(columnNames).contains("isCompleted")
            assertThat(columnNames).contains("completedAt")
            assertThat(columnNames).contains("createdAt")

            cursor.close()
            close()
        }
    }

    @Test
    fun createDatabaseVersion1_canInsertData() {
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data directly using SQL
            execSQL("""
                INSERT INTO todos (title, description, isCompleted, completedAt, createdAt)
                VALUES ('Test Todo', 'Description', 0, NULL, ${System.currentTimeMillis()})
            """.trimIndent())

            // Verify data was inserted
            val cursor = query("SELECT COUNT(*) FROM todos")
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            assertThat(count).isEqualTo(1)

            cursor.close()
            close()
        }
    }

    @Test
    fun createDatabaseVersion1_autoGeneratesPrimaryKey() {
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert without specifying id
            execSQL("""
                INSERT INTO todos (title, description, isCompleted, completedAt, createdAt)
                VALUES ('Todo 1', '', 0, NULL, 1000)
            """.trimIndent())
            execSQL("""
                INSERT INTO todos (title, description, isCompleted, completedAt, createdAt)
                VALUES ('Todo 2', '', 0, NULL, 2000)
            """.trimIndent())

            // Verify IDs were auto-generated
            val cursor = query("SELECT id FROM todos ORDER BY id")
            cursor.moveToFirst()
            val id1 = cursor.getLong(0)
            cursor.moveToNext()
            val id2 = cursor.getLong(0)

            assertThat(id1).isEqualTo(1)
            assertThat(id2).isEqualTo(2)

            cursor.close()
            close()
        }
    }

    /**
     * Helper to get a migrated Room database for verification.
     * Use this after running migrations to verify data integrity.
     */
    private fun getMigratedRoomDatabase(): AppDataBase {
        return Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDataBase::class.java,
            TEST_DB
        ).build()
    }
}