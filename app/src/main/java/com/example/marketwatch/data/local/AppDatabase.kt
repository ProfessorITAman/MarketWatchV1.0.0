package com.example.marketwatch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.marketwatch.data.local.FavoriteDao

@Database(
    entities = [
        FavoriteEntity::class,
        InstrumentEntity::class
    ], 
    version = 2, 
    exportSchema = false 
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun instrumentDao(): InstrumentDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //Migration
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE favorites_new (
                    symbol TEXT PRIMARY KEY,
                    name TEXT,
                    price REAL NOT NULL DEFAULT 0.0,
                    changePercent REAL NOT NULL DEFAULT 0.0,
                    timestamp INTEGER NOT NULL DEFAULT 0,
                    lastUpdated INTEGER NOT NULL DEFAULT 0
                )
            """
                )
                db.execSQL(
                    """
                INSERT INTO favorites_new (symbol, name, price, changePercent, timestamp, lastUpdated)
                SELECT symbol, name, 
                       CAST(price AS REAL), 
                       CAST(REPLACE(changePercent, '%', '') AS REAL),
                       timestamp, ${System.currentTimeMillis()}
                FROM favorites
            """
                )
                db.execSQL("DROP TABLE favorites")
                db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketwatch_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
