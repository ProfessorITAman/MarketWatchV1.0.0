package com.example.marketwatch.presentaion.viewmodels.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * ContextWrapper для динамической смены языка приложения.
 *
 * Применяется ко всем экранам Compose/Activity для локализации.
 * Поддерживает ru/en + RTL/LTR направление текста.
 *
 * Использование: LocaleContextWrapper.wrap(context, "ru")
 */
class LocaleContextWrapper(base: Context) : ContextWrapper(base) //{

  //  companion object {
        /**
         * Создать локализованный Context для Activity/Compose.
         *
         * @param context базовый Context (Activity/Application)
         * @param language "ru" или "en" из SettingsRepository
         * @return Context с нужным языком + направлением
         */
        //wrap
   //     fun wrap(context: Context, language: String): ContextWrapper {
    //        val lang = if (language == "ru") "ru" else "en"  // ✅ Только ru/en

    //        val locale = Locale.forLanguageTag(lang)  // ✅ Современный API (API 21+)

            // Глобально устанавливаем локаль (для DateFormat, etc.)
     //       Locale.setDefault(locale)

            // Создаем новую конфигурацию
  //          val config = Configuration(context.resources.configuration)
  //          config.setLocale(locale)           // ✅ Язык (ru_RU, en_US)
  //          config.setLayoutDirection(locale)  // ✅ Направление (LTR→RTL для арабского)

            // Новый Context с локализацией
 //           val localizedContext = context.createConfigurationContext(config)
 //           return LocaleContextWrapper(localizedContext)
 //       }
  //  }
//}