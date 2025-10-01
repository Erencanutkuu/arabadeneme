package com.erencanutku.arabaoyunu2

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class GameManager(
    private val context: Context,
    private val scoreText: TextView,
    private val levelText: TextView
) {
    private var currentLevel: Level? = null
    private var currentQuestionIndex = 0
    private var correctAnswersInLevel = 0
    private var questions: List<Question> = emptyList()
    private var maxQuestionsInLevel = 0
    private var score = 0
    private var lives = GameConfig.INITIAL_LIVES
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
    fun getLives(): Int = lives
    fun getScore(): Int = score

    fun loseLife(): Boolean {
        lives--
        if (lives < 0) lives = 0
        updateScore()
        return lives == 0
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
            Toast.makeText(context, "✅ Doğru! +1 puan", Toast.LENGTH_SHORT).show()
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
            lives--
            Toast.makeText(context, "❌ Yanlış! Can: $lives", Toast.LENGTH_SHORT).show()

            if (lives <= 0) {
                isGameRunning = false
                Toast.makeText(context, "💀 Oyun bitti!", Toast.LENGTH_LONG).show()
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
        scoreText.text = "Skor: $score | Doğru: $correctAnswersInLevel/$maxQuestionsInLevel"
    }

    private fun updateLevelDisplay() {
        levelText.text = "Level ${currentLevel!!.levelNumber}"
    }

    fun resetGame() {
        score = 0
        lives = GameConfig.INITIAL_LIVES
        isGameRunning = true
        updateScore()
    }

    fun stopGame() {
        isGameRunning = false
    }
} 