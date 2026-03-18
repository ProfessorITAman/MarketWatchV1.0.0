package com.example.marketwatch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность Room для таблицы избранных акций (favorites).
 *
 * Хранит оффлайн данные котировок для быстрого доступа без сети.
 * Каждый тикер уникален (PRIMARY KEY = symbol).
 *
 * Таблица автоматически создается Room по аннотации @Entity.
 *
 * @see FavoriteDao CRUD операции
 * @see AppDatabase содержит эту таблицу
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(

    /**
     * PRIMARY KEY = уникальный тикер акции (AAPL, GOOGL, MSFT).
     *
     * Room автоматически индексирует PRIMARY KEY.
     * insert() с REPLACE заменяет всю запись по symbol.
     * delete(symbol) удаляет по этому полю.
     */
    @PrimaryKey val symbol: String,

    val name: String? = null,

    /**
     * Текущая цена акции на момент добавления в избранное.
     *
     * Double для математических операций (сравнение, сортировка).
     * Приходит из GlobalQuote.price (String → Double.parseDouble()).
     */
    val price: Double,

    /**
     * Процентное изменение цены на момент добавления.
     *
     * String сохраняет оригинальный формат API ("+1.69%", "-0.85%").
     * Включает знак и символ % для UI отображения.
     */
    val changePercent: Double,

    /**
     * Timestamp добавления в избранное (Unix milliseconds).
     *
     * По умолчанию = System.currentTimeMillis() при создании.
     * Используется для сортировки (новые акции сверху).
     *
     * Пример: 1645091234567 → сортировка ORDER BY timestamp DESC
     */
    val timestamp: Long = System.currentTimeMillis(),

    val lastUpdated: Long = 0L
)