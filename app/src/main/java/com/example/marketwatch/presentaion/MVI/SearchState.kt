package com.example.marketwatch.presentaion.MVI

import com.example.marketwatch.data.remote.dto.GlobalQuote

/**
 * Состояние экрана поиска котировок акций (UI State).
 *
 * Используется в MVI архитектуре для управления состоянием ViewModel и UI.
 * Data class позволяет легко сравнивать состояния (equals()), копировать (copy())
 * и отлаживать (toString() с автоматическим выводом всех полей).
 *
 * Паттерн Unidirectional Data Flow (UDF): одно неизменяемое состояние содержит
 * все данные для рендера UI + статус загрузки + ошибки.
 *
 * @property quote данные котировки (null пока не загружены)
 * @property isLoading true во время запроса к API, false иначе
 * @property error текст ошибки (null если успешно)
 * @property ticker введенный пользователем тикер акции (AAPL, GOOGL)
 */

data class SearchState(
    /**
     * Данные котировки из API. null до завершения успешного запроса.
     * Заполняется объектом GlobalQuote после ответа QuoteResponse.
     */
    val quote: GlobalQuote? = null,

    /**
     * Флаг загрузки. Показывает ProgressBar/Spinner когда true.
     * UI реагирует: isLoading = true → показать лоадер, скрыть контент.
     */
    val isLoading: Boolean = false,

    /**
     * Текст ошибки для показа пользователю (Toast/Snackbar).
     * null = нет ошибки. Устанавливается при сетевых ошибках или 404.
     */
    val error: String? = null,

    /**
     * Тикер акции введенный пользователем в EditText.
     * Хранит текущее значение поля ввода для восстановления UI состояния.
     */
    val ticker: String = ""
)