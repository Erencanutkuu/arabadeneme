package com.erencanutku.arabaoyunu2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class GameSession(
    val date: String,
    val duration: Long, // milliseconds
    val score: Int,
    val level: Int,
    val wordsCorrect: Int,
    val wordsWrong: Int,
    val collisions: Int,
    val characterUsed: String
)

data class PlayerStats(
    val totalPlayTime: Long,
    val totalGamesPlayed: Int,
    val averageScore: Float,
    val bestScore: Int,
    val totalWordsLearned: Int,
    val accuracy: Float,
    val favoriteCharacter: Character?,
    val currentStreak: Int,
    val longestStreak: Int,
    val dailyGoalProgress: Float,
    val weeklyGoalProgress: Float,
    val levelsCompleted: Int,
    val totalCollisions: Int,
    val totalDistance: Float
)

object AnalyticsSystem {

    fun recordGameSession(
        context: Context,
        duration: Long,
        score: Int,
        level: Int,
        wordsCorrect: Int,
        wordsWrong: Int,
        collisions: Int
    ) {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Bugünün tarihi
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val characterSystem = CharacterSystem(context)
        val selectedCharacter = characterSystem.getSelectedCharacter().id

        // Session kaydet
        val session = GameSession(
            date = today,
            duration = duration,
            score = score,
            level = level,
            wordsCorrect = wordsCorrect,
            wordsWrong = wordsWrong,
            collisions = collisions,
            characterUsed = selectedCharacter
        )

        saveGameSession(context, session)

        // Günlük istatistikleri güncelle
        updateDailyStats(context, session)

        // Streak'i güncelle
        updateStreak(context, today)

        // Toplam istatistikleri güncelle
        val totalPlayTime = prefs.getLong("total_play_time", 0) + duration
        val totalGames = prefs.getInt("total_games", 0) + 1
        val totalWordsCorrect = prefs.getInt("total_words_correct", 0) + wordsCorrect
        val totalWordsWrong = prefs.getInt("total_words_wrong", 0) + wordsWrong
        val totalCollisions = prefs.getInt("total_collisions", 0) + collisions

        editor.putLong("total_play_time", totalPlayTime)
        editor.putInt("total_games", totalGames)
        editor.putInt("total_words_correct", totalWordsCorrect)
        editor.putInt("total_words_wrong", totalWordsWrong)
        editor.putInt("total_collisions", totalCollisions)
        editor.putInt("total_words_learned", totalWordsCorrect + totalWordsWrong)

        // En iyi skor
        val currentBest = prefs.getInt("personal_best_score", 0)
        if (score > currentBest) {
            editor.putInt("personal_best_score", score)
        }

        // Perfect score sayacý
        if (wordsWrong == 0 && wordsCorrect > 0) {
            val perfectCount = prefs.getInt("perfect_score_count", 0)
            editor.putInt("perfect_score_count", perfectCount + 1)
        }

        editor.apply()

        // Leaderboard'a ekle
        LeaderboardSystem.addScore(context, score, level, wordsCorrect)
    }

    private fun saveGameSession(context: Context, session: GameSession) {
        val prefs = context.getSharedPreferences("GameSessions", Context.MODE_PRIVATE)
        val sessionsJson = prefs.getString("sessions_data", "[]") ?: "[]"
        val sessionsArray = JSONArray(sessionsJson)

        val sessionObj = JSONObject().apply {
            put("date", session.date)
            put("duration", session.duration)
            put("score", session.score)
            put("level", session.level)
            put("wordsCorrect", session.wordsCorrect)
            put("wordsWrong", session.wordsWrong)
            put("collisions", session.collisions)
            put("characterUsed", session.characterUsed)
        }

        sessionsArray.put(sessionObj)

        // Son 30 günü tut
        if (sessionsArray.length() > 100) {
            val newArray = JSONArray()
            for (i in (sessionsArray.length() - 100) until sessionsArray.length()) {
                newArray.put(sessionsArray.get(i))
            }
            prefs.edit().putString("sessions_data", newArray.toString()).apply()
        } else {
            prefs.edit().putString("sessions_data", sessionsArray.toString()).apply()
        }
    }

    private fun updateDailyStats(context: Context, session: GameSession) {
        val prefs = context.getSharedPreferences("DailyStats", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val today = session.date

        val dailyScore = prefs.getInt("daily_score_$today", 0) + session.score
        val dailyWords = prefs.getInt("daily_words_$today", 0) + session.wordsCorrect
        val dailyGames = prefs.getInt("daily_games_$today", 0) + 1

        editor.putInt("daily_score_$today", dailyScore)
        editor.putInt("daily_words_$today", dailyWords)
        editor.putInt("daily_games_$today", dailyGames)
        editor.putString("last_play_date", today)
        editor.apply()
    }

    private fun updateStreak(context: Context, today: String) {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val lastPlayDate = prefs.getString("last_play_date", "")
        val currentStreak = prefs.getInt("daily_play_streak", 0)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val newStreak = when {
            lastPlayDate == yesterday -> currentStreak + 1
            lastPlayDate == today -> currentStreak
            else -> 1
        }

        val longestStreak = prefs.getInt("longest_streak", 0)
        val editor = prefs.edit()

        editor.putInt("daily_play_streak", newStreak)
        if (newStreak > longestStreak) {
            editor.putInt("longest_streak", newStreak)
        }
        editor.apply()
    }

    fun getPlayerStats(context: Context): PlayerStats {
        val prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE)

        val totalPlayTime = prefs.getLong("total_play_time", 0)
        val totalGames = prefs.getInt("total_games", 0)
        val totalWordsCorrect = prefs.getInt("total_words_correct", 0)
        val totalWordsWrong = prefs.getInt("total_words_wrong", 0)
        val totalWords = totalWordsCorrect + totalWordsWrong

        val accuracy = if (totalWords > 0) {
            (totalWordsCorrect.toFloat() / totalWords) * 100
        } else 0f

        val averageScore = if (totalGames > 0) {
            prefs.getInt("total_score", 0).toFloat() / totalGames
        } else 0f

        return PlayerStats(
            totalPlayTime = totalPlayTime,
            totalGamesPlayed = totalGames,
            averageScore = averageScore,
            bestScore = prefs.getInt("personal_best_score", 0),
            totalWordsLearned = prefs.getInt("total_words_learned", 0),
            accuracy = accuracy,
            favoriteCharacter = CharacterSystem(context).getSelectedCharacter(),
            currentStreak = prefs.getInt("daily_play_streak", 0),
            longestStreak = prefs.getInt("longest_streak", 0),
            dailyGoalProgress = getDailyGoalProgress(context),
            weeklyGoalProgress = getWeeklyGoalProgress(context),
            levelsCompleted = LevelSystem.getHighestLevel(context),
            totalCollisions = prefs.getInt("total_collisions", 0),
            totalDistance = prefs.getFloat("total_distance", 0f)
        )
    }

    fun getDailyGoalProgress(context: Context): Float {
        val prefs = context.getSharedPreferences("DailyStats", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyWords = prefs.getInt("daily_words_$today", 0)
        val dailyGoal = 50 // Günlük hedef 50 kelime

        return (dailyWords.toFloat() / dailyGoal) * 100
    }

    fun getWeeklyGoalProgress(context: Context): Float {
        val prefs = context.getSharedPreferences("DailyStats", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        var weeklyWords = 0

        // Son 7 günün verilerini topla
        for (i in 0..6) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            weeklyWords += prefs.getInt("daily_words_$date", 0)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val weeklyGoal = 300 // Haftalık hedef 300 kelime
        return (weeklyWords.toFloat() / weeklyGoal) * 100
    }

    fun getWeeklyChart(context: Context): List<Pair<String, Int>> {
        val prefs = context.getSharedPreferences("DailyStats", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        val weekData = mutableListOf<Pair<String, Int>>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            val wordsLearned = prefs.getInt("daily_words_$date", 0)

            weekData.add(Pair(dayName, wordsLearned))
        }

        return weekData
    }

    fun getMostUsedCharacter(context: Context): Character? {
        val sessions = getRecentSessions(context, 30)
        val characterUsage = sessions.groupBy { it.characterUsed }
            .mapValues { it.value.size }
            .maxByOrNull { it.value }

        return characterUsage?.let { usage -> CharacterSystem(context).getAllCharacters().find { char: Character -> char.id == usage.key } }
    }

    private fun getRecentSessions(context: Context, days: Int): List<GameSession> {
        val prefs = context.getSharedPreferences("GameSessions", Context.MODE_PRIVATE)
        val sessionsJson = prefs.getString("sessions_data", "[]") ?: "[]"
        val sessionsArray = JSONArray(sessionsJson)

        val sessions = mutableListOf<GameSession>()
        for (i in 0 until sessionsArray.length()) {
            val obj = sessionsArray.getJSONObject(i)
            sessions.add(
                GameSession(
                    date = obj.getString("date"),
                    duration = obj.getLong("duration"),
                    score = obj.getInt("score"),
                    level = obj.getInt("level"),
                    wordsCorrect = obj.getInt("wordsCorrect"),
                    wordsWrong = obj.getInt("wordsWrong"),
                    collisions = obj.getInt("collisions"),
                    characterUsed = obj.getString("characterUsed")
                )
            )
        }

        return sessions.takeLast(days)
    }
}