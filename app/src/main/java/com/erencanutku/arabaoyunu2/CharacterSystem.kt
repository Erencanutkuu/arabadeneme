package com.erencanutku.arabaoyunu2

import android.content.Context

data class Character(
    val id: String,
    val name: String,
    val emoji: String,
    val ability: String,
    val unlockRequirement: String,
    val cost: Int = 0,
    val isUnlocked: Boolean = false,
    val speedBonus: Float = 1.0f,
    val fuelEfficiency: Float = 1.0f,
    val wordBonus: Float = 1.0f
)

class CharacterSystem(private val context: Context) {

    private val allCharacters = listOf(
        Character(
            id = "starter",
            name = "Başlangıç Sürücüsü",
            emoji = "🚗",
            ability = "Başlangıç bonusu +10 puan",
            unlockRequirement = "Başlangıçta mevcut",
            cost = 0,
            isUnlocked = true,
            speedBonus = 1.0f,
            fuelEfficiency = 1.0f,
            wordBonus = 1.0f
        ),
        Character(
            id = "speedy",
            name = "Hızlı Hans",
            emoji = "⚡",
            ability = "+20% hız bonusu",
            unlockRequirement = "Level 3'e ulaş",
            cost = 0,
            isUnlocked = false,
            speedBonus = 1.2f,
            fuelEfficiency = 1.0f,
            wordBonus = 1.0f
        ),
        Character(
            id = "teacher",
            name = "Bilge Öğretmen",
            emoji = "👨‍🏫",
            ability = "+50% kelime puanı",
            unlockRequirement = "50 kelime öğren",
            cost = 0,
            isUnlocked = false,
            speedBonus = 1.0f,
            fuelEfficiency = 1.0f,
            wordBonus = 1.5f
        ),
        Character(
            id = "explorer",
            name = "Gezgin",
            emoji = "🗺️",
            ability = "Daha az yakıt tüketimi",
            unlockRequirement = "Level 5'e ulaş",
            cost = 0,
            isUnlocked = false,
            speedBonus = 1.0f,
            fuelEfficiency = 0.8f,
            wordBonus = 1.0f
        ),
        Character(
            id = "ninja",
            name = "Ninja",
            emoji = "🥷",
            ability = "Çarpışma hasarı -30%",
            unlockRequirement = "5 mükemmel skor",
            cost = 0,
            isUnlocked = false,
            speedBonus = 1.0f,
            fuelEfficiency = 0.7f,
            wordBonus = 1.0f
        ),
        Character(
            id = "rocket",
            name = "Roket Adam",
            emoji = "🚀",
            ability = "Süper hız bonusu",
            unlockRequirement = "Level 10'a ulaş",
            cost = 0,
            isUnlocked = false,
            speedBonus = 0.8f,
            fuelEfficiency = 1.0f,
            wordBonus = 1.2f
        ),
        Character(
            id = "cat",
            name = "Kedi Şoförü",
            emoji = "🐱",
            ability = "Ekstra şans bonusu",
            unlockRequirement = "7 gün üst üste oyna",
            cost = 0,
            isUnlocked = false,
            speedBonus = 1.0f,
            fuelEfficiency = 0.9f,
            wordBonus = 1.1f
        ),
        Character(
            id = "robot",
            name = "Robot",
            emoji = "🤖",
            ability = "Tüm bonuslar +25%",
            unlockRequirement = "100 kelime öğren",
            cost = 0,
            isUnlocked = false,
            speedBonus = 0.9f,
            fuelEfficiency = 0.8f,
            wordBonus = 1.3f
        )
    )

    fun getAllCharacters(): List<Character> {
        return allCharacters.map { character ->
            character.copy(isUnlocked = isCharacterUnlocked(character))
        }
    }

    fun getSelectedCharacter(): Character {
        val selectedId = getSelectedCharacterId()
        return getAllCharacters().find { it.id == selectedId } ?: getAllCharacters().first()
    }

    fun getSelectedCharacterId(): String {
        val prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        return prefs.getString("selected_character", "starter") ?: "starter"
    }

    fun selectCharacter(characterId: String) {
        val prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_character", characterId).apply()
    }

    fun updateUnlockStatus(currentLevel: Int, wordsLearned: Int, perfectScores: Int, dailyStreak: Int, playerMoney: Int) {
        // This method updates the game stats but doesn't change the unlock logic
        // The unlock status is checked dynamically in isCharacterUnlocked
    }

    private fun isCharacterUnlocked(character: Character): Boolean {
        if (character.id == "starter") return true

        val prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val currentLevel = prefs.getInt("current_level", 1)
        val wordsLearned = prefs.getInt("words_learned", 0)
        val perfectScores = prefs.getInt("perfect_scores", 0)
        val dailyStreak = prefs.getInt("daily_streak", 0)

        return when (character.id) {
            "speedy" -> currentLevel >= 3
            "teacher" -> wordsLearned >= 50
            "explorer" -> currentLevel >= 5
            "ninja" -> perfectScores >= 5
            "rocket" -> currentLevel >= 10
            "cat" -> dailyStreak >= 7
            "robot" -> wordsLearned >= 100
            else -> false
        }
    }
}