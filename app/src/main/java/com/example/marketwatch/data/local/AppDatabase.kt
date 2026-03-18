package com.example.marketwatch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.marketwatch.data.local.FavoriteDao


/**
 * Room база данных для хранения избранных акций пользователя.
 *
 * Содержит таблицу FavoriteEntity с тикерами (AAPL, GOOGL) для оффлайн доступа.
 * Singleton паттерн обеспечивает одну БД на приложение.
 *
 * Архитектура: UI → Repository → DAO → Room Database
 *
 * @see FavoriteEntity таблица избранных акций
 * @see FavoriteDao CRUD операции с избранными
 */
@Database(
    entities = [
        FavoriteEntity::class,
        InstrumentEntity::class
    ], // ✅ Таблицы БД (сущности)
    version = 2,  // ✅ Версия схемы (миграции при изменении)
    exportSchema = false  // ✅ Отключает схему для миграций (debug)
)

abstract class AppDatabase : RoomDatabase() {// ✅ Преобразователи типов (Date, enum)

    /**
     * Абстрактный метод возвращает DAO для работы с избранными акциями.
     * Room генерирует реализацию автоматически во время компиляции.
     */
    abstract fun favoriteDao(): FavoriteDao
    abstract fun instrumentDao(): InstrumentDao

    /**
     * Singleton для thread-safe инициализации БД.
     *
     * Паттерн Double-checked locking + @Volatile:
     * 1. INSTANCE проверяется без блокировки
     * 2. synchronized(this) создает БД один раз
     * 3. @Volatile обеспечивает видимость изменений между потоками
     */
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
                    .addMigrations(MIGRATION_1_2)  // ЭТО РЕШАЕТ КРАШ!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}