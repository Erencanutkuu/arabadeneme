package com.erencanutku.arabaoyunu2

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView


class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager //dışardan gelecek degerler için laneinit kullanılır null olmicak ama oncreat içinde tanımlayacagımız şeyler
    private lateinit var handler: Handler
    private lateinit var gameRunnable: Runnable
    private lateinit var carController: CarController
    private lateinit var lottieSuccess: LottieAnimationView


    private lateinit var carImage: ImageView
    private lateinit var obstacles: Array<ImageView>
    private lateinit var obstacleY: FloatArray
    private lateinit var obstacleX: FloatArray

    private lateinit var option1: Button
    private lateinit var option2: Button
    private lateinit var questionText: TextView
    private lateinit var scoreText: TextView
    private lateinit var levelText: TextView
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var fuelProgressBar: ProgressBar

    private var answerHandled = false
    private var collisionHandled = false
    private var currentQuestion: Question? = null
    private var isQuestionMode = true
    private var questionCooldown = 600
    private var currentCooldown = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Test için level 1'den başla
        LevelSystem.setCurrentLevel(this, 1)

        initializeViews()

        gameManager = GameManager(this, scoreText, levelText, fuelProgressBar)
        gameManager.initializeLevel()

        carController = CarController(
            carImage,
            rootLayout,
            gameManager
        ) {
            Toast.makeText(this, "⛔️ Daha fazla gidemezsin!", Toast.LENGTH_SHORT).show()
        }

        setupGestureDetection()
        handler = Handler(Looper.getMainLooper())
        startGame()
    }

    override fun onResume() {
        super.onResume()

        // 💰 Para bilgisi güncellemesi (geri dönünce güncellensin diye)
        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val money = prefs.getInt("money", 0)

        // Eğer layout'ta moneyText yoksa bu satır hata verir, o yüzden try-catch ile saralım:
        try {
            findViewById<TextView>(R.id.moneyText).text = "💰 $money"
        } catch (e: Exception) {
            e.printStackTrace() // Geliştirici hatası varsa logda görelim
        }
    }


    private fun initializeViews() {
        rootLayout = findViewById(R.id.rootLayout)
        carImage = findViewById(R.id.carImage)

        // 🔽 Seçilen arabayı ayarla
        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val selectedCar = prefs.getString("selectedCar", "car1")

        when (selectedCar) {
            "car1" -> carImage.setImageResource(R.drawable.arabaagorsel)
            "car2" -> carImage.setImageResource(R.drawable.arabaengel)
            "car3" -> carImage.setImageResource(R.drawable.arabagorsel)
        }
        lottieSuccess = findViewById(R.id.lottieSuccess)

        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        questionText = findViewById(R.id.questionText)
        scoreText = findViewById(R.id.scoreText)
        levelText = findViewById(R.id.levelText)
        lottieSuccess = findViewById(R.id.lottieSuccess)
        fuelProgressBar = findViewById(R.id.fuelProgressBar)

        obstacles = arrayOf(
            findViewById(R.id.obstacle),
            findViewById(R.id.obstacle2),
            findViewById(R.id.obstacle3)
        )

        obstacleY = FloatArray(obstacles.size) { -400f * (it + 1) }
        obstacleX = FloatArray(obstacles.size) { 100f + (it * 200f) }
    }

    private fun startGame() {
        // Sadece oyunun en başında çağrılmalı
        gameManager.resetGame()
        restartLevel()
    }


    private fun startGameLoop() {
        gameRunnable = object : Runnable {
            override fun run() {
                if (!gameManager.isGameRunning()) return
                val speed = gameManager.getObstacleSpeed() * 1.5f
                moveObstacles(speed)
                if (isQuestionMode) moveQuestions(speed) else hideQuestions()
                checkCollisions()

                // Sürekli yakıt tüketimi (her frame'de küçük miktar)
                val isGameOver = gameManager.loseFuel(0.05f) // Her 16ms'de 0.05% yakıt tüketimi
                if (isGameOver) {
                    endGame()
                    return
                }

                currentCooldown++
                if (currentCooldown >= questionCooldown) {
                    isQuestionMode = !isQuestionMode
                    currentCooldown = 0
                    if (isQuestionMode) spawnQuestion()
                }
                handler.postDelayed(this, 16)
            }
        }
        handler.post(gameRunnable)
    }

    private fun moveObstacles(speed: Float) {
        for (i in obstacles.indices) {
            obstacleY[i] += speed
            if (obstacleY[i] > rootLayout.height + obstacles[i].height) {
                obstacleY[i] = -obstacles[i].height.toFloat() - 400f
                obstacleX[i] = 100f + (Math.random() * (rootLayout.width - 200)).toFloat()
            }
            obstacles[i].y = obstacleY[i]
            obstacles[i].x = obstacleX[i]
        }
    }

    private fun moveQuestions(speed: Float) {
        option1.translationY += speed
        option2.translationY += speed
        if (option1.translationY > rootLayout.height) option1.alpha = 0f
        if (option2.translationY > rootLayout.height) option2.alpha = 0f
    }

    private fun hideQuestions() {
        option1.alpha = 0f
        option2.alpha = 0f
    }

    private fun checkCollisions() {
        val q = currentQuestion ?: return
        val carX = carImage.x
        val carY = carImage.y
        val carW = carImage.width
        val carH = carImage.height

        for (i in obstacles.indices) {
            val obs = obstacles[i]
            if (isColliding(carX, carY, carW, carH, obs.x, obs.y, obs.width, obs.height)) {
                if (!collisionHandled) {
                    collisionHandled = true
                    val isGameOver = gameManager.loseFuel(15f) // Çarpışmada 15% yakıt kaybı
                    Toast.makeText(this, "💥 Çarpışma! -15% Yakıt", Toast.LENGTH_SHORT).show()
                    if (isGameOver) endGame()
                    else handler.postDelayed({ collisionHandled = false }, 1500)
                }
                return
            }
        }

        if (!answerHandled && isQuestionMode) {
            if (isColliding(carX, carY, carW, carH, option1.x, option1.y, option1.width, option1.height)) {
                answerHandled = true
                processAnswer(option1.text.toString() == q.correct)
            } else if (isColliding(carX, carY, carW, carH, option2.x, option2.y, option2.width, option2.height)) {
                answerHandled = true
                processAnswer(option2.text.toString() == q.correct)
            }
        }
    }

    private fun isColliding(x1: Float, y1: Float, w1: Int, h1: Int, x2: Float, y2: Float, w2: Int, h2: Int): Boolean {
        // Araba şeklinde hitbox: arabanın gerçek şekline göre ayarlanmış
        val carWidthPadding = 20f // Yanlarda daha fazla boşluk (arabanın oval şekli için)
        val carHeightPadding = 8f // Üst ve altta daha az boşluk (arabanın uzun şekli için)
        val obstaclePadding = 12f // Engellerin kenarlarından bu kadar pixel içerde hitbox

        // Arabanın gerçek hitbox koordinatları (araba şeklinde: yanlarda dar, uzunlamasına geniş)
        val carLeft = x1 + carWidthPadding
        val carRight = x1 + w1 - carWidthPadding
        val carTop = y1 + carHeightPadding
        val carBottom = y1 + h1 - carHeightPadding

        // Engelin hitbox koordinatları
        val obstacleLeft = x2 + obstaclePadding
        val obstacleRight = x2 + w2 - obstaclePadding
        val obstacleTop = y2 + obstaclePadding
        val obstacleBottom = y2 + h2 - obstaclePadding

        // Çarpışma kontrolü
        return !(carRight < obstacleLeft || carLeft > obstacleRight || carBottom < obstacleTop || carTop > obstacleBottom)
    }

    private fun processAnswer(isCorrect: Boolean) {
        option1.alpha = 0.3f
        option2.alpha = 0.3f

        val targetView = if (option1.text.toString() == currentQuestion?.correct) option1 else option2

        if (isCorrect) {
            animateCorrectAnswer(targetView)
        } else {
            animateShake(option1)
            animateShake(option2)
        }

        gameManager.handleAnswer(
            isCorrect,
            onLevelCompleted = {
                handler.removeCallbacks(gameRunnable)

                // 💰 Para ekle
                val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
                val currentMoney = prefs.getInt("money", 0)
                val newMoney = currentMoney + 50
                prefs.edit().putInt("money", newMoney).apply()

                Toast.makeText(this, "💰 +50 Kazandın!", Toast.LENGTH_SHORT).show()

                // 🔁 Ana menüye dön
                startActivity(Intent(this, MainMenuActivity::class.java))
                finish()
            },
            onGameOver = {
                endGame()
            }
        )

        // Yakıt durumunu güncelle (GameManager'da otomatik güncelleniyor)

        // Butonları eski haline döndür
        handler.postDelayed({
            option1.alpha = 1f
            option2.alpha = 1f
            answerHandled = false
        }, 500)
    }


    private fun spawnQuestion() {
        val question = gameManager.loadQuestion()
        currentQuestion = question
        val left = (0..1).random() == 0
        questionText.text = "What is '${question.word}' in Turkish?"
        if (left) {
            option1.text = question.correct
            option2.text = question.wrong
        } else {
            option1.text = question.wrong
            option2.text = question.correct
        }
        option1.translationY = -200f
        option2.translationY = -200f
        option1.alpha = 1f
        option2.alpha = 1f
    }

    private fun endGame() {
        gameManager.stopGame()
        handler.removeCallbacks(gameRunnable)

        val intent = Intent(this, MainMenuActivity::class.java)
        intent.putExtra("LEVEL_COMPLETED", true) // ← ANİMASYON TETİKLEYİCİ
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameManager.stopGame()
        handler.removeCallbacks(gameRunnable)
    }
    private fun setupGestureDetection() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                val deltaX = e2.x - e1.x
                if (kotlin.math.abs(deltaX) > 50) {
                    if (deltaX > 0) carController.moveRight() else carController.moveLeft()
                    return true
                }
                return false
            }
        })

        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }
    private fun animateShake(view: View) {
        val shake = TranslateAnimation(0f, 12f, 0f, 0f).apply {
            duration = 80
            repeatMode = Animation.REVERSE
            repeatCount = 4
        }
        view.startAnimation(shake)
    }
    private fun animateCorrectAnswer(view: View) {
        val scale = ScaleAnimation(
            1f, 1.2f, 1f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
        view.startAnimation(scale)
    }

    private fun restartLevel() {
        answerHandled = false
        collisionHandled = false
        spawnQuestion()
        startGameLoop()
    }

    private fun showLevelCompleteAnimation() {
        Log.d("LEVEL", "🎬 showLevelCompleteAnimation çağrıldı")

        lottieSuccess.visibility = View.VISIBLE
        lottieSuccess.bringToFront()
        lottieSuccess.playAnimation()

        lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Animasyon başlarken yapılacak bir şey yok
            }

            override fun onAnimationEnd(animation: Animator) {
                // 💰 PARA EKLEME BLOĞU
                val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
                val currentMoney = prefs.getInt("money", 0)
                val newMoney = currentMoney + 50
                prefs.edit().putInt("money", newMoney).apply()

                // 🎉 TOAST MESAJI
                Toast.makeText(this@MainActivity, "💰 +50 Kazandın!", Toast.LENGTH_SHORT).show()

                // Animasyonu gizle ve oyunu yeniden başlat
                lottieSuccess.visibility = View.GONE
                startGame()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }


    private fun showLevelCompleteAnimationWithReward() {
        Log.d("DEBUG_LEVEL", "🎬 Animasyon fonksiyonu çağrıldı")

        lottieSuccess.visibility = View.VISIBLE
        lottieSuccess.bringToFront()
        lottieSuccess.playAnimation()

        lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // 🔥 PARA EKLEME BURAYA ALINDI!
                val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
                val currentMoney = prefs.getInt("money", 0)
                val newMoney = currentMoney + 50
                prefs.edit().putInt("money", newMoney).apply()
                Toast.makeText(this@MainActivity, "💰 +50 Kazandın!", Toast.LENGTH_SHORT).show()

                // Oyun tekrar başlasın
                lottieSuccess.visibility = View.GONE
                startGame()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }



}






