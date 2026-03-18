package com.example.marketwatch.ui.theme


import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import java.util.Locale


val LocalLanguage = compositionLocalOf { "ru" }

@Composable
fun LanguageProvider(
    language: String,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLanguage provides language) {
        content()
    }
}

@Composable
fun stringResourceWithLocale(resId: Int): String {
    val language = LocalLanguage.current
    val context = LocalContext.current

    val locale = Locale(language)  // ✅ java.util.Locale
    val config = Configuration(context.resources.configuration).apply {
        setLocale(locale)
    }

    val localizedContext = context.createConfigurationContext(config)
    return localizedContext.resources.getString(resId)
}

