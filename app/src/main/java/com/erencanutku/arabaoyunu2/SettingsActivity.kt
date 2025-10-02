package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var soundSwitch: Switch
    private lateinit var vibrationSwitch: Switch
    private lateinit var backupSwitch: Switch
    private lateinit var notificationSwitch: Switch
    private lateinit var backButton: TextView
    private lateinit var languageSelector: LinearLayout
    private lateinit var accountLinking: LinearLayout
    private lateinit var selectedLanguage: TextView
    private lateinit var accountStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kayıtlı dili uygula
        applySavedLanguage()

        setContentView(R.layout.activity_settings_simple)

        Log.d("SETTINGS_DEBUG", "onCreate başladı")

        try {
            initializeViews()
            Log.d("SETTINGS_DEBUG", "initializeViews tamam")

            loadSettings()
            Log.d("SETTINGS_DEBUG", "loadSettings tamam")

            setupListeners()
            Log.d("SETTINGS_DEBUG", "setupListeners tamam")
        } catch (e: Exception) {
            Log.e("SETTINGS_ERROR", "Hata: ${e.message}", e)
            Toast.makeText(this, "Ayarlar yüklenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        try {
            soundSwitch = findViewById(R.id.soundSwitch)
            Log.d("SETTINGS_DEBUG", "soundSwitch bulundu")

            vibrationSwitch = findViewById(R.id.vibrationSwitch)
            Log.d("SETTINGS_DEBUG", "vibrationSwitch bulundu")

            backupSwitch = findViewById(R.id.backupSwitch)
            Log.d("SETTINGS_DEBUG", "backupSwitch bulundu")

            notificationSwitch = findViewById(R.id.notificationSwitch)
            Log.d("SETTINGS_DEBUG", "notificationSwitch bulundu")

            backButton = findViewById(R.id.backButton)
            Log.d("SETTINGS_DEBUG", "backButton bulundu")

            languageSelector = findViewById(R.id.languageSelector)
            Log.d("SETTINGS_DEBUG", "languageSelector bulundu")

            accountLinking = findViewById(R.id.accountLinking)
            Log.d("SETTINGS_DEBUG", "accountLinking bulundu")

            selectedLanguage = findViewById(R.id.selectedLanguage)
            Log.d("SETTINGS_DEBUG", "selectedLanguage bulundu")

            accountStatus = findViewById(R.id.accountStatus)
            Log.d("SETTINGS_DEBUG", "accountStatus bulundu")

        } catch (e: Exception) {
            Log.e("SETTINGS_ERROR", "findViewById hatası: ${e.message}", e)
            throw e
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        vibrationSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)
        backupSwitch.isChecked = prefs.getBoolean("backup_enabled", false)
        notificationSwitch.isChecked = prefs.getBoolean("notifications_enabled", true)

        val selectedLang = prefs.getString("selected_language", "tr")
        val languageDisplayName = getLanguageDisplayName(selectedLang ?: "tr")
        selectedLanguage.text = languageDisplayName

        val isAccountLinked = prefs.getBoolean("account_linked", false)
        accountStatus.text = if (isAccountLinked) {
            getString(R.string.account_status_linked)
        } else {
            getString(R.string.account_status_unlinked)
        }
    }

    private fun setupListeners() {
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("sound_enabled", isChecked)
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("vibration_enabled", isChecked)
        }

        backupSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("backup_enabled", isChecked)
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("notifications_enabled", isChecked)
        }

        languageSelector.setOnClickListener {
            showLanguageSelectionDialog()
        }

        accountLinking.setOnClickListener {
            showAccountLinkingDialog()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun saveSettings(key: String, value: Boolean) {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    private fun saveStringSettings(key: String, value: String) {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    private fun showLanguageSelectionDialog() {
        val languageCodes = arrayOf("tr", "en", "ar", "fr", "de", "es")
        val languageNames = arrayOf(
            getString(R.string.language_turkish),
            getString(R.string.language_english),
            getString(R.string.language_arabic),
            getString(R.string.language_french),
            getString(R.string.language_german),
            getString(R.string.language_spanish)
        )

        val currentLang = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
            .getString("selected_language", "tr")
        val currentIndex = languageCodes.indexOf(currentLang)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language_dialog_title))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLangCode = languageCodes[which]
                val selectedLangName = languageNames[which]

                saveStringSettings("selected_language", selectedLangCode)

                // Dili değiştir ve uygulamayı yeniden başlat
                showRestartDialog(selectedLangName)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showAccountLinkingDialog() {
        val isLinked = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
            .getBoolean("account_linked", false)

        if (isLinked) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.account_management_title))
                .setMessage(getString(R.string.account_unlink_message))
                .setPositiveButton(getString(R.string.unlink_account)) { _, _ ->
                    saveSettings("account_linked", false)
                    accountStatus.text = getString(R.string.account_status_unlinked)
                    Toast.makeText(this, getString(R.string.account_unlinked), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.account_linking))
                .setMessage(getString(R.string.account_link_message))
                .setPositiveButton(getString(R.string.link_with_google)) { _, _ ->
                    saveSettings("account_linked", true)
                    accountStatus.text = getString(R.string.account_status_linked)
                    Toast.makeText(this, getString(R.string.account_linked_success), Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton(getString(R.string.link_with_facebook)) { _, _ ->
                    saveSettings("account_linked", true)
                    accountStatus.text = getString(R.string.account_status_linked)
                    Toast.makeText(this, getString(R.string.account_linked_success), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
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

    private fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "tr" -> getString(R.string.language_turkish)
            "en" -> getString(R.string.language_english)
            "ar" -> getString(R.string.language_arabic)
            "fr" -> getString(R.string.language_french)
            "de" -> getString(R.string.language_german)
            "es" -> getString(R.string.language_spanish)
            else -> getString(R.string.language_turkish)
        }
    }

    private fun showRestartDialog(selectedLanguage: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language_dialog_title))
            .setMessage(getString(R.string.language_restart_required))
            .setPositiveButton(getString(R.string.restart_app)) { _, _ ->
                restartApp()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // Kullanıcı iptal ederse, seçili dili güncelle ama yeniden başlatma
                val savedLang = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
                    .getString("selected_language", "tr") ?: "tr"
                this.selectedLanguage.text = getLanguageDisplayName(savedLang)
                Toast.makeText(this, getString(R.string.language_changed, selectedLanguage), Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartApp() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}