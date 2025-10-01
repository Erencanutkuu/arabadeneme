package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val startButton = findViewById<Button>(R.id.buttonStart)
        val levelPathContainer = findViewById<LinearLayout>(R.id.levelPathContainer)
        val currentLevelText = findViewById<TextView>(R.id.currentLevelText)
        val activeLevelTitle = findViewById<TextView>(R.id.activeLevelTitle)
        val activeLevelDesc = findViewById<TextView>(R.id.activeLevelDesc)
        val progressText = findViewById<TextView>(R.id.progressText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Mevcut leveli göster
        val currentLevel = LevelSystem.getCurrentLevel(this)
        val highestLevel = LevelSystem.getHighestLevel(this)
        val totalLevels = LevelSystem.getAllLevels().size

        currentLevelText.text = "Mevcut Level: $currentLevel | En Yüksek Level: $highestLevel"

        // İlerleme durumunu güncelle
        progressText.text = "İlerleme: $highestLevel/$totalLevels"
        progressBar.max = totalLevels
        progressBar.progress = highestLevel

        // Aktif level bilgilerini güncelle
        updateActiveLevelInfo(activeLevelTitle, activeLevelDesc, currentLevel)

        // Ana oyun butonu
        startButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Aktif level bloğuna tıklama
        findViewById<LinearLayout>(R.id.activeLevelBlock).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Duolingo tarzı level path'i oluştur
        createLevelPath(levelPathContainer)
    }

    override fun onResume() {
        super.onResume()

        // 💰 Para güncellemesini her dönüşte yap
        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val money = prefs.getInt("money", 0)
        Log.d("PARA_KONTROL", "🪙 Mevcut para: $money")
        findViewById<TextView>(R.id.moneyText).text = "💰 $money"
    }


    private fun updateActiveLevelInfo(title: TextView, desc: TextView, level: Int) {
        val levelData = LevelSystem.getLevel(level)
        title.text = "$level. SEVİYE"
        
        val descriptions = mapOf(
            1 to "Temel kelimeleri öğren",
            2 to "Günlük hayat kelimeleri",
            3 to "Doğa ve çevre",
            4 to "Meslekler ve iş",
            5 to "Yerler ve mekanlar",
            6 to "Sıfatlar ve özellikler",
            7 to "Zaman ifadeleri",
            8 to "Yemek ve içecek",
            9 to "Spor ve aktiviteler",
            10 to "Teknoloji ve gelecek"
        )
        
        desc.text = descriptions[level] ?: "Kelime öğren"
    }

    private fun createLevelPath(container: LinearLayout) {
        val allLevels = LevelSystem.getAllLevels()
        val highestLevel = LevelSystem.getHighestLevel(this)

        for (i in allLevels.indices) {
            val level = allLevels[i]

            // FrameLayout (Button + Animasyon üst üste)
            val levelFrame = FrameLayout(this).apply {
                val size = resources.getDimensionPixelSize(R.dimen.level_button_size)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 12, 0, 12)
                }
            }

            // Level butonu
            val levelButton = Button(this).apply {
                text = level.levelNumber.toString()
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                background = ContextCompat.getDrawable(context, R.drawable.level_node)
                isEnabled = LevelSystem.isLevelUnlocked(context, level.levelNumber)

                setOnClickListener {
                    LevelSystem.setCurrentLevel(context, level.levelNumber)
                    startActivity(Intent(context, MainActivity::class.java))
                }
            }

            // Tik animasyonu (Lottie)
            val tickAnim = LottieAnimationView(this).apply {
                setAnimation("correct_anim.json")
                repeatCount = 0
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                visibility = if (level.levelNumber < highestLevel) View.VISIBLE else View.GONE
                if (visibility == View.VISIBLE) playAnimation()
            }

            // Buton altta, animasyon üstte
            levelButton.elevation = 5f
            tickAnim.elevation = 10f

            levelFrame.addView(levelButton)
            levelFrame.addView(tickAnim)
            container.addView(levelFrame)

            // Bağlayıcı çizgi yerine animasyon
            if (i < allLevels.size - 1) {
                val connectorAnim = LottieAnimationView(this).apply {
                    setAnimation("question_linear.json")
                    repeatCount = LottieDrawable.INFINITE
                    layoutParams = LinearLayout.LayoutParams(
                        200,  // genişlik (çizgi kalınlığı gibi)
                        200  // yükseklik (çizgi uzunluğu gibi)
                    ).apply {
                        gravity = Gravity.CENTER
                        setMargins(0, 8, 0, 8)
                    }
                    playAnimation()
                }
                container.addView(connectorAnim)
            }

        }
    }



}

