package com.ravaroj.habitcurrency.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ravaroj.habitcurrency.data.local.dao.DailyTaskTemplateDao
import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.RewardDao
import com.ravaroj.habitcurrency.data.local.dao.TagDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateEntity
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateTagCrossRef
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardTagCrossRef
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskTagCrossRef

@Database(
    entities = [
        TaskInstanceEntity::class,
        DailyTaskTemplateEntity::class,
        RewardEntity::class,
        RedemptionEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        RewardTagCrossRef::class,
        DailyTaskTemplateTagCrossRef::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun dailyTaskTemplateDao(): DailyTaskTemplateDao
    abstract fun rewardDao(): RewardDao
    abstract fun redemptionDao(): RedemptionDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add displayOrder to TaskInstanceEntity
                db.execSQL(
                    "ALTER TABLE task_instances ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0"
                )
                // Add displayOrder to RewardEntity
                db.execSQL(
                    "ALTER TABLE rewards ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0"
                )

                // Backfill displayOrder with dense ranks matching the existing sort.
                normalizeTaskDisplayOrders(db)
                normalizeRewardDisplayOrders(db)
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                normalizeTaskDisplayOrders(db)
                normalizeRewardDisplayOrders(db)
                createOrderIndexes(db)
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS task_tag_cross_ref (
                        taskId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        PRIMARY KEY(taskId, tagId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_task_tag_cross_ref_taskId ON task_tag_cross_ref(taskId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_task_tag_cross_ref_tagId ON task_tag_cross_ref(tagId)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createRewardTagTable(db)
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_task_template_tag_cross_ref (
                        templateId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        PRIMARY KEY(templateId, tagId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_daily_task_template_tag_cross_ref_templateId ON daily_task_template_tag_cross_ref(templateId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_daily_task_template_tag_cross_ref_tagId ON daily_task_template_tag_cross_ref(tagId)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_currency_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                    .also { Instance = it }
            }
        }

        private fun normalizeTaskDisplayOrders(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                UPDATE task_instances
                SET displayOrder = (
                    SELECT COUNT(*)
                    FROM task_instances AS ordered_tasks
                    WHERE ordered_tasks.date = task_instances.date
                        AND (
                            ordered_tasks.displayOrder < task_instances.displayOrder
                            OR (
                                ordered_tasks.displayOrder = task_instances.displayOrder
                                AND ordered_tasks.createdAt > task_instances.createdAt
                            )
                            OR (
                                ordered_tasks.displayOrder = task_instances.displayOrder
                                AND ordered_tasks.createdAt = task_instances.createdAt
                                AND ordered_tasks.id <= task_instances.id
                            )
                        )
                )
                """.trimIndent()
            )
        }

        private fun normalizeRewardDisplayOrders(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                UPDATE rewards
                SET displayOrder = (
                    SELECT COUNT(*)
                    FROM rewards AS ordered_rewards
                    WHERE ordered_rewards.displayOrder < rewards.displayOrder
                        OR (
                            ordered_rewards.displayOrder = rewards.displayOrder
                            AND ordered_rewards.createdAt > rewards.createdAt
                        )
                        OR (
                            ordered_rewards.displayOrder = rewards.displayOrder
                            AND ordered_rewards.createdAt = rewards.createdAt
                            AND ordered_rewards.id <= rewards.id
                        )
                )
                """.trimIndent()
            )
        }

        private fun createOrderIndexes(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_task_instances_date_displayOrder_createdAt ON task_instances(date, displayOrder, createdAt)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_rewards_isActive_displayOrder_createdAt ON rewards(isActive, displayOrder, createdAt)"
            )
        }

        private fun createRewardTagTable(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reward_tag_cross_ref (
                    rewardId INTEGER NOT NULL,
                    tagId INTEGER NOT NULL,
                    PRIMARY KEY(rewardId, tagId)
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reward_tag_cross_ref_rewardId ON reward_tag_cross_ref(rewardId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reward_tag_cross_ref_tagId ON reward_tag_cross_ref(tagId)"
            )
        }
    }
}
