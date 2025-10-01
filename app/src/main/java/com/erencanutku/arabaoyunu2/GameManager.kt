package com.erencanutku.arabaoyunu2

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class GameManager(
    private val context: Context,
    private val scoreText: TextView,
    private val levelText: TextView,
    private val fuelProgressBar: ProgressBar? = null
) {
    private var currentLevel: Level? = null
    private var currentQuestionIndex = 0
    private var correctAnswersInLevel = 0
    private var questions: List<Question> = emptyList()
    private var maxQuestionsInLevel = 0
    private var score = 0
    private var fuel = 100f // Yakıt seviyesi (0-100)
    private var obstacleSpeed = 7f
    private var isGameRunning = true

    fun initializeLevel() {
        currentLevel = LevelSystem.getCurrentLevelData(context)
        if (currentLevel == null) {
            LevelSystem.setCurrentLevel(context, 1)
            currentLevel = LevelSystem.getCurrentLevelData(context)
        }
        
        questions = currentLevel!!.questions
        maxQuestionsInLevel = currentLevel!!.requiredCorrectAnswers
        currentQuestionIndex = 0
        correctAnswersInLevel = 0
        obstacleSpeed = 7f + (currentLevel!!.levelNumber - 1) * 2f
        
        updateLevelDisplay()
    }

    fun getCurrentLevel(): Level? = currentLevel
    fun getQuestions(): List<Question> = questions
    fun getCurrentQuestionIndex(): Int = currentQuestionIndex
    fun getObstacleSpeed(): Float = obstacleSpeed
    fun isGameRunning(): Boolean = isGameRunning
    fun getFuel(): Float = fuel
    fun getScore(): Int = score

    fun loseFuel(amount: Float = 15f): Boolean {
        fuel -= amount
        if (fuel < 0f) fuel = 0f
        updateScore()
        return fuel == 0f
    }

    fun addFuel(amount: Float = 20f) {
        fuel += amount
        if (fuel > 100f) fuel = 100f
        updateScore()
    }


    fun loadQuestion(): Question {
        if (currentQuestionIndex >= questions.size) {
            currentQuestionIndex = 0
        }
        return questions[currentQuestionIndex]
    }

    fun handleAnswer(isCorrect: Boolean, onLevelCompleted: () -> Unit, onGameOver: () -> Unit) {
        if (!isGameRunning) return

        if (isCorrect) {
            score++
            correctAnswersInLevel++
            addFuel(20f) // Doğru cevapta 20% yakıt ekle
            Toast.makeText(context, "✅ Doğru! +20% Yakıt", Toast.LENGTH_SHORT).show()
            Log.d("DEBUG_LEVEL", "✅ Doğru cevaplandı. correctAnswersInLevel = $correctAnswersInLevel / $maxQuestionsInLevel")

            if (correctAnswersInLevel >= maxQuestionsInLevel) {
                isGameRunning = false
                levelCompleted(onLevelCompleted)
                return
            }

            currentQuestionIndex++
            if (currentQuestionIndex >= questions.size) {
                currentQuestionIndex = 0
            }

        } else {
            val fuelLoss = 25f // Yanlış cevapta daha fazla yakıt kaybı
            fuel -= fuelLoss
            if (fuel < 0f) fuel = 0f
            Toast.makeText(context, "❌ Yanlış! -25% Yakıt", Toast.LENGTH_SHORT).show()

            if (fuel <= 0f) {
                isGameRunning = false
                Toast.makeText(context, "⛽ Yakıt bitti! Oyun sona erdi!", Toast.LENGTH_LONG).show()
                onGameOver()
                return
            }
        }

        updateScore()
    }


    private fun levelCompleted(onLevelCompleted: () -> Unit) {
        isGameRunning = false
        Log.d("DEBUG_LEVEL", "🎉 levelCompleted fonksiyonu tetiklendi")

        val currentLevelNumber = currentLevel!!.levelNumber
        LevelSystem.unlockNextLevel(context) // Sadece bir sonraki level'in kilidini aç

        Toast.makeText(context, "🎉 Level $currentLevelNumber tamamlandı!", Toast.LENGTH_LONG).show()
        Log.d("LEVEL", "Level tamamlandı! Ana menüye dönülecek")

        // Otomatik olarak bir sonraki level'i başlatma!
        // Sadece callback’i tetikle → animasyon veya ana menüye dönüş burada yapılır
        onLevelCompleted()
    }


    private fun updateScore() {
        scoreText.text = "⛽ ${fuel.toInt()}% | Doğru: $correctAnswersInLevel/$maxQuestionsInLevel"
        fuelProgressBar?.progress = fuel.toInt()

        // Yakıt seviyesine göre renk değiştir
        fuelProgressBar?.let { progressBar ->
            val color = when {
                fuel > 50f -> android.graphics.Color.parseColor("#4CAF50") // Yeşil
                fuel > 25f -> android.graphics.Color.parseColor("#FF9800") // Turuncu
                else -> android.graphics.Color.parseColor("#F44336") // Kırmızı
            }
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
        }
    }

    private fun updateLevelDisplay() {
        levelText.text = "Level ${currentLevel!!.levelNumber}"
    }

    fun resetGame() {
        score = 0
        fuel = 100f
        isGameRunning = true
        updateScore()
    }

    fun stopGame() {
        isGameRunning = false
    }
} 