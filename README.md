# MarketWatch Android 📱💹 (AlphaVantage API)

Android-приложение для получения котировок акций через **AlphaVantage API** с **Clean Architecture + MVI**.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-orange.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-14+-green.svg)](https://developer.android.com)
[![AlphaVantage](https://img.shields.io/badge/AlphaVantage-API-blue.svg)](https://www.alphavantage.co)

## Быстрый запуск

```bash
git clone https://github.com/ProfessorITAman/MarketWatchV1.0.0.git
cd MarketWatch
./gradlew build
./gradlew installDebug

Требования
Android Studio Koala | 2024.1.1+
Android SDK 34+
Kotlin 1.9.20+

API Key (AlphaVantage)
Получите БЕСПЛАТНЫЙ ключ:
alphavantage.co
"Get Free API Key" → Email подтверждение (1 минута)
# local.properties (НЕ коммитьте в Git!)
ALPHA_VANTAGE_API_KEY=your_key_here
const val API_KEY = BuildConfig.ALPHA_VANTAGE_API_KEY
Бесплатные лимиты: 25 запросов/день, 5/мин

Технологический стек:
Kotlin
Jetpack Compose (UI)
MVI (единый поток данных: Intent → Reducer → State)
Clean Architecture
Retrofit + OkHttp (сеть)
Room (локальная БД)
Koin (DI)
SharedPreferences
DataStore

Архитектура
┌─ Compose UI (Jetpack Compose + MVI)
│  ↓ StateFlow (Loading/Data/Error)
├─ ViewModel (SearchStockIntent → StockState)
│  ↓ UseCase
├─ Domain (GetStockQuoteUseCase)
│  ↓ Repository
└─ Data Layer
   ├── AlphaVantageApi (Retrofit)
   ├── Room (кэш 24ч)
   └── DataStore (API key)

Проблемы:
AlphaVantage лимит 25 req/day
JSON парсинг complex response
Network ошибки
Поиск по тикеру

Решение:
Room кэш (24ч) + offline-first
Debounce поиск + очередь запросов
Moshi + data classes с @Json
Retry (3 попытки) + exponential backoff
SYMBOL_SEARCH endpoint + dropdown

API Endpoints (AlphaVantage):
// Текущая цена акции
GLOBAL_QUOTE?symbol=IBM&apikey=${API_KEY}
// Поиск тикера  
SYMBOL_SEARCH?keywords=Apple&apikey=${API_KEY}

Тестирование
./gradlew testDebugUnitTest    # MockWebServer + UseCase тесты
./gradlew connectedCheck       # UI + Network тесты

MarketWatch/
├── app/              # Navigation + DI (Koin)
├── :data/            # Retrofit + Room + Repository
├── :domain/          # UseCase + Models (StockQuote)
├── :core/            # Extensions + Utils
├── .gitignore
└── README.md    ← Вы здесь!



