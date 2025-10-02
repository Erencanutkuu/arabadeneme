package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class CharacterSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kayıtlı dili uygula
        applySavedLanguage()

        setContentView(R.layout.activity_character_selection)

        // Back button
        findViewById<android.widget.TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // TODO: Implement character selection functionality
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", "tr") ?: "tr"
        setLocale(savedLanguage)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}