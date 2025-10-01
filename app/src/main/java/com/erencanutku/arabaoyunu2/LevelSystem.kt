package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.SharedPreferences

data class Level(
    val levelNumber: Int,
    val questions: List<Question>,
    val requiredCorrectAnswers: Int,
    val isUnlocked: Boolean = false
)

object LevelSystem {
    private const val PREFS_NAME = "GameProgress"
    private const val KEY_CURRENT_LEVEL = "currentLevel"
    private const val KEY_HIGHEST_LEVEL = "highestLevel"
    
    private val levels = listOf(
        Level(1, listOf(
            Question("apple", "elma", "armut"),
            Question("car", "araba", "uçak"),
            Question("house", "ev", "okul"),
            Question("book", "kitap", "kalem"),
            Question("cat", "kedi", "köpek"),
            Question("sun", "güneş", "ay"),
            Question("tree", "ağaç", "çiçek"),
            Question("water", "su", "çay"),
            Question("food", "yemek", "içecek"),
            Question("friend", "arkadaş", "aile")
        ), 7, true),
        
        Level(2, listOf(
            Question("school", "okul", "ev"),
            Question("computer", "bilgisayar", "telefon"),
            Question("music", "müzik", "film"),
            Question("color", "renk", "şekil"),
            Question("time", "zaman", "saat"),
            Question("work", "iş", "oyun"),
            Question("family", "aile", "arkadaş"),
            Question("money", "para", "altın"),
            Question("country", "ülke", "şehir"),
            Question("language", "dil", "kelime")
        ), 8),
        
        Level(3, listOf(
            Question("weather", "hava", "güneş"),
            Question("season", "mevsim", "ay"),
            Question("holiday", "tatil", "iş"),
            Question("birthday", "doğum günü", "yılbaşı"),
            Question("telephone", "telefon", "bilgisayar"),
            Question("window", "pencere", "kapı"),
            Question("river", "nehir", "göl"),
            Question("mountain", "dağ", "tepe"),
            Question("forest", "orman", "çöl"),
            Question("island", "ada", "yarımada")
        ), 1),
        
        Level(4, listOf(
            Question("teacher", "öğretmen", "öğrenci"),
            Question("student", "öğrenci", "öğretmen"),
            Question("doctor", "doktor", "hemşire"),
            Question("engineer", "mühendis", "doktor"),
            Question("police", "polis", "asker"),
            Question("fireman", "itfaiyeci", "polis"),
            Question("driver", "şoför", "yolcu"),
            Question("pilot", "pilot", "hostes"),
            Question("farmer", "çiftçi", "işçi"),
            Question("chef", "aşçı", "garson")
        ), 1),
        
        Level(5, listOf(
            Question("university", "üniversite", "lise"),
            Question("library", "kütüphane", "müze"),
            Question("hospital", "hastane", "eczane"),
            Question("airport", "havaalanı", "istasyon"),
            Question("station", "istasyon", "havaalanı"),
            Question("market", "market", "mağaza"),
            Question("restaurant", "restoran", "kafe"),
            Question("hotel", "otel", "pansiyon"),
            Question("museum", "müze", "kütüphane"),
            Question("theater", "tiyatro", "sinema")
        ), 1)
    )
    
    fun getCurrentLevel(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_LEVEL, 1)
    }
    
    fun getHighestLevel(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_HIGHEST_LEVEL, 1)
    }
    
    fun setCurrentLevel(context: Context, level: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CURRENT_LEVEL, level).apply()
        
        // En yüksek leveli güncelle
        val highestLevel = getHighestLevel(context)
        if (level > highestLevel) {
            prefs.edit().putInt(KEY_HIGHEST_LEVEL, level).apply()
        }
    }
    
    fun getLevel(levelNumber: Int): Level? {
        return levels.find { it.levelNumber == levelNumber }
    }
    
    fun getCurrentLevelData(context: Context): Level? {
        return getLevel(getCurrentLevel(context))
    }
    
    fun isLevelUnlocked(context: Context, levelNumber: Int): Boolean {
        if (levelNumber == 1) return true
        // Sadece önceki leveli tamamlamışsa açılır
        val highestLevel = getHighestLevel(context)
        return highestLevel >= levelNumber
    }
    
    fun unlockNextLevel(context: Context) {
        val currentLevel = getCurrentLevel(context)
        val highestLevel = getHighestLevel(context)
        
        if (currentLevel == highestLevel && currentLevel < levels.size) {
            setCurrentLevel(context, currentLevel + 1)
        }
    }
    
    fun getAllLevels(): List<Level> = levels
} 