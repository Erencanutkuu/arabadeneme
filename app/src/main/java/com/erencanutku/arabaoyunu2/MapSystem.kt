package com.erencanutku.arabaoyunu2

import android.content.Context

data class MapTheme(
    val id: String,
    val name: String,
    val backgroundResource: Int,
    val unlockedAtLevel: Int,
    val description: String
)

object MapSystem {
    private const val PREFS_NAME = "MapSettings"
    private const val KEY_SELECTED_MAP = "selectedMap"

    private val mapThemes = listOf(
        MapTheme(
            id = "city",
            name = "🏙️ Şehir",
            backgroundResource = R.drawable.map_city,
            unlockedAtLevel = 1,
            description = "Asfalt yollar ve bina silüetleri"
        ),
        MapTheme(
            id = "forest",
            name = "🌲 Orman",
            backgroundResource = R.drawable.map_forest,
            unlockedAtLevel = 3,
            description = "Toprak yollar ve yeşil doğa"
        ),
        MapTheme(
            id = "desert",
            name = "🏜️ Çöl",
            backgroundResource = R.drawable.map_desert,
            unlockedAtLevel = 5,
            description = "Kumsal yollar ve sıcak güneş"
        ),
        MapTheme(
            id = "snow",
            name = "❄️ Kar",
            backgroundResource = R.drawable.map_snow,
            unlockedAtLevel = 7,
            description = "Buzlu yollar ve kar taneleri"
        )
    )

    fun getAllMaps(): List<MapTheme> = mapThemes

    fun getSelectedMap(context: Context): MapTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedMapId = prefs.getString(KEY_SELECTED_MAP, "city")
        return mapThemes.find { it.id == selectedMapId } ?: mapThemes[0]
    }

    fun setSelectedMap(context: Context, mapId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_MAP, mapId).apply()
    }

    fun isMapUnlocked(context: Context, mapTheme: MapTheme): Boolean {
        val highestLevel = LevelSystem.getHighestLevel(context)
        return highestLevel >= mapTheme.unlockedAtLevel
    }

    fun getMapForLevel(level: Int): MapTheme {
        return when {
            level >= 7 -> mapThemes[3] // Kar
            level >= 5 -> mapThemes[2] // Çöl
            level >= 3 -> mapThemes[1] // Orman
            else -> mapThemes[0] // Şehir
        }
    }
}