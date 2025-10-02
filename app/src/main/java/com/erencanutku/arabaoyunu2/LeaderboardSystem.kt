package com.erencanutku.arabaoyunu2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LeaderboardEntry(
    val playerName: String,
    val score: Int,
    val level: Int,
    val wordsLearned: Int,
    val characterId: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

object LeaderboardSystem {

    fun addScore(context: Context, score: Int, level: Int, wordsLearned: Int) {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val playerName = prefs.getString("player_name", "Oyuncu") ?: "Oyuncu"
        val characterId = prefs.getInt("selected_character", 1)

        val entry = LeaderboardEntry(playerName, score, level, wordsLearned, characterId)
        saveToLeaderboard(context, entry)

        // Kişisel en yüksek skoru güncelle
        val personalBest = prefs.getInt("personal_best_score", 0)
        if (score > personalBest) {
            prefs.edit().putInt("personal_best_score", score).apply()
        }
    }

    private fun saveToLeaderboard(context: Context, entry: LeaderboardEntry) {
        val prefs = context.getSharedPreferences("Leaderboard", Context.MODE_PRIVATE)
        val leaderboardJson = prefs.getString("leaderboard_data", "[]") ?: "[]"

        val leaderboardArray = JSONArray(leaderboardJson)
        val newEntry = JSONObject().apply {
            put("playerName", entry.playerName)
            put("score", entry.score)
            put("level", entry.level)
            put("wordsLearned", entry.wordsLearned)
            put("characterId", entry.characterId)
            put("timestamp", entry.timestamp)
        }

        leaderboardArray.put(newEntry)

        // En iyi 50 skoru tut
        val sortedList = mutableListOf<JSONObject>()
        for (i in 0 until leaderboardArray.length()) {
            sortedList.add(leaderboardArray.getJSONObject(i))
        }

        sortedList.sortByDescending { it.getInt("score") }
        val topEntries = sortedList.take(50)

        val finalArray = JSONArray()
        topEntries.forEach { finalArray.put(it) }

        prefs.edit().putString("leaderboard_data", finalArray.toString()).apply()
    }

    fun getTopScores(context: Context, limit: Int = 10): List<LeaderboardEntry> {
        val prefs = context.getSharedPreferences("Leaderboard", Context.MODE_PRIVATE)
        val leaderboardJson = prefs.getString("leaderboard_data", "[]") ?: "[]"
        val leaderboardArray = JSONArray(leaderboardJson)

        val entries = mutableListOf<LeaderboardEntry>()
        for (i in 0 until minOf(leaderboardArray.length(), limit)) {
            val obj = leaderboardArray.getJSONObject(i)
            entries.add(
                LeaderboardEntry(
                    playerName = obj.getString("playerName"),
                    score = obj.getInt("score"),
                    level = obj.getInt("level"),
                    wordsLearned = obj.getInt("wordsLearned"),
                    characterId = obj.optInt("characterId", 1),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }

        return entries.sortedByDescending { it.score }
    }

    fun getWeeklyTopScores(context: Context, limit: Int = 10): List<LeaderboardEntry> {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return getTopScores(context, 50).filter { it.timestamp >= weekAgo }.take(limit)
    }

    fun getPlayerRank(context: Context): Int {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val personalBest = prefs.getInt("personal_best_score", 0)
        val allScores = getTopScores(context, 50)

        val rank = allScores.indexOfFirst { it.score <= personalBest } + 1
        return if (rank == 0) allScores.size + 1 else rank
    }
}