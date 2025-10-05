package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CharacterSelectionActivity : AppCompatActivity() {
    private lateinit var characterSystem: CharacterSystem
    private lateinit var characterAdapter: CharacterAdapter
    private lateinit var moneyText: TextView
    private lateinit var selectedCharacterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kayıtlı dili uygula
        applySavedLanguage()

        setContentView(R.layout.activity_character_selection)

        // Initialize character system
        characterSystem = CharacterSystem(this)

        // Initialize views
        moneyText = findViewById(R.id.moneyText)
        selectedCharacterText = findViewById(R.id.selectedCharacterName)
        val charactersRecyclerView = findViewById<RecyclerView>(R.id.charactersRecyclerView)

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        charactersRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Update character unlock status based on current progress
        updateCharacterUnlockStatus()

        // Create adapter
        characterAdapter = CharacterAdapter(
            this,
            characterSystem.getAllCharacters(),
            characterSystem.getSelectedCharacterId(),
            onCharacterSelected = { character ->
                selectCharacter(character)
            }
        )
        charactersRecyclerView.adapter = characterAdapter

        // Update UI
        updateUI()
    }

    private fun updateCharacterUnlockStatus() {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val currentLevel = prefs.getInt("current_level", 1)
        val wordsLearned = prefs.getInt("words_learned", 0)
        val perfectScores = prefs.getInt("perfect_scores", 0)
        val dailyStreak = prefs.getInt("daily_streak", 0)
        val playerMoney = prefs.getInt("player_money", 100)

        characterSystem.updateUnlockStatus(currentLevel, wordsLearned, perfectScores, dailyStreak, playerMoney)
    }

    private fun selectCharacter(character: Character) {
        if (character.isUnlocked) {
            characterSystem.selectCharacter(character.id)

            // Update adapter
            characterAdapter = CharacterAdapter(
                this,
                characterSystem.getAllCharacters(),
                characterSystem.getSelectedCharacterId(),
                onCharacterSelected = { char ->
                    selectCharacter(char)
                }
            )
            findViewById<RecyclerView>(R.id.charactersRecyclerView).adapter = characterAdapter

            updateUI()
        }
    }

    private fun updateUI() {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val playerMoney = prefs.getInt("player_money", 100)
        val selectedCharacter = characterSystem.getSelectedCharacter()

        moneyText.text = "$playerMoney"
        selectedCharacterText.text = selectedCharacter.name
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