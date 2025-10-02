package com.erencanutku.arabaoyunu2

import android.content.Context

data class Character(
    val id: Int,
    val name: String,
    val emoji: String,
    val description: String,
    val unlockRequirement: String,
    val cost: Int = 0,
    val isUnlocked: Boolean = false,
    val specialAbility: String = ""
)

object CharacterSystem {

    private val allCharacters = listOf(
        Character(
            id = 1,
            name = "Başlangıç Sürücüsü",
            emoji = "😊",
            description = "Öğrenmeye hevesli genç sürücü",
            unlockRequirement = "Varsayılan karakter",
            cost = 0,
            specialAbility = "Başlangıç bonusu +10 puan"
        ),
        Character(
            id = 2,
            name = "Hızlı Hans",
            emoji = "🏎️",
            description = "Hız tutkuny professional racer",
            unlockRequirement = "Level 3'e ulaş",
            cost = 100,
            specialAbility = "Hız bonusu +15%"
        ),
        Character(
            id = 3,
            name = "Bilge Öğretmen",
            emoji = "👨‍🏫",
            description = "Kelime ustasý academic",
            unlockRequirement = "100 kelime öğren",
            cost = 150,
            specialAbility = "Kelime bonusu +20 puan"
        ),
        Character(
            id = 4,
            name = "Gezgin",
            emoji = "🧳",
            description = "Dünyayý gezen polyglot",
            unlockRequirement = "3 farklı dil kullan",
            cost = 200,
            specialAbility = "Çoklu dil bonusu +25%"
        ),
        Character(
            id = 5,
            name = "Ninja",
            emoji = "🥷",
            description = "Gizli kelime ninja",
            unlockRequirement = "Perfect skor 5 kez",
            cost = 250,
            specialAbility = "Yakıt tüketimi -%20"
        ),
        Character(
            id = 6,
            name = "Roket Adam",
            emoji = "🚀",
            description = "Uzaydan gelen süper sürücü",
            unlockRequirement = "Level 10'a ulaş",
            cost = 300,
            specialAbility = "Tüm bonuslar +30%"
        ),
        Character(
            id = 7,
            name = "Kedi Şoförü",
            emoji = "🐱",
            description = "Sevimli kedi sürücü",
            unlockRequirement = "7 gün üst üste oyna",
            cost = 180,
            specialAbility = "Can bonusu +1"
        ),
        Character(
            id = 8,
            name = "Robot",
            emoji = "🤖",
            description = "AI destekli süper sürücü",
            unlockRequirement = "1000 puan skora ulaş",
            cost = 350,
            specialAbility = "Otomatik doğru cevap %10"
        )
    )

    fun getAllCharacters(context: Context): List<Character> {
        return allCharacters.map { character ->
            character.copy(isUnlocked = isCharacterUnlocked(context, character.id))
        }
    }

    fun getUnlockedCharacters(context: Context): List<Character> {
        return getAllCharacters(context).filter { it.isUnlocked }
    }

    fun isCharacterUnlocked(context: Context, characterId: Int): Boolean {
        if (characterId == 1) return true // Varsayılan karakter

        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val unlockedCharacters = prefs.getStringSet("unlocked_characters", setOf("1")) ?: setOf("1")

        if (unlockedCharacters.contains(characterId.toString())) return true

        // Unlock koşullarını kontrol et
        val character = allCharacters.find { it.id == characterId } ?: return false

        return when (characterId) {
            2 -> LevelSystem.getHighestLevel(context) >= 3
            3 -> getTotalWordsLearned(context) >= 100
            4 -> getLanguagesUsed(context) >= 3
            5 -> getPerfectScoreCount(context) >= 5
            6 -> LevelSystem.getHighestLevel(context) >= 10
            7 -> getDailyPlayStreak(context) >= 7
            8 -> prefs.getInt("personal_best_score", 0) >= 1000
            else -> false
        }
    }

    fun unlockCharacter(context: Context, characterId: Int): Boolean {
        if (isCharacterUnlocked(context, characterId)) return true

        val character = allCharacters.find { it.id == characterId } ?: return false
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val money = prefs.getInt("money", 0)

        if (money >= character.cost) {
            // Parayı düş
            prefs.edit().putInt("money", money - character.cost).apply()

            // Karakteri unlock et
            val unlockedCharacters = prefs.getStringSet("unlocked_characters", setOf("1"))?.toMutableSet() ?: mutableSetOf("1")
            unlockedCharacters.add(characterId.toString())
            prefs.edit().putStringSet("unlocked_characters", unlockedCharacters).apply()

            return true
        }
        return false
    }

    fun setSelectedCharacter(context: Context, characterId: Int) {
        if (isCharacterUnlocked(context, characterId)) {
            val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
            prefs.edit().putInt("selected_character", characterId).apply()
        }
    }

    fun getSelectedCharacter(context: Context): Character {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val selectedId = prefs.getInt("selected_character", 1)
        return allCharacters.find { it.id == selectedId } ?: allCharacters[0]
    }

    fun getCharacterBonus(context: Context, baseScore: Int): Int {
        val selectedCharacter = getSelectedCharacter(context)
        return when (selectedCharacter.id) {
            1 -> baseScore + 10
            2 -> (baseScore * 1.15f).toInt()
            3 -> baseScore + 20
            4 -> (baseScore * 1.25f).toInt()
            5 -> baseScore // Yakıt bonusu oyunda uygulanır
            6 -> (baseScore * 1.30f).toInt()
            7 -> baseScore // Can bonusu oyunda uygulanır
            8 -> baseScore // Otomatik cevap oyunda uygulanır
            else -> baseScore
        }
    }

    // Helper functions
    private fun getTotalWordsLearned(context: Context): Int {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        return prefs.getInt("total_words_learned", 0)
    }

    private fun getLanguagesUsed(context: Context): Int {
        val prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val usedLanguages = prefs.getStringSet("languages_used", setOf()) ?: setOf()
        return usedLanguages.size
    }

    private fun getPerfectScoreCount(context: Context): Int {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        return prefs.getInt("perfect_score_count", 0)
    }

    private fun getDailyPlayStreak(context: Context): Int {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        return prefs.getInt("daily_play_streak", 0)
    }
}