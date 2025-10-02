package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import java.util.Locale

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kayıtlı dili uygula
        applySavedLanguage()

        setContentView(R.layout.activity_main_menu_simple)

        val startButton = findViewById<Button>(R.id.buttonStart)
        val levelPathContainer = findViewById<LinearLayout>(R.id.levelPathContainer)
        val currentLevelText = findViewById<TextView>(R.id.currentLevelText)
        val activeLevelTitle = findViewById<TextView>(R.id.activeLevelTitle)
        val activeLevelDesc = findViewById<TextView>(R.id.activeLevelDesc)
        val progressText = findViewById<TextView>(R.id.progressText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val settingsButton = findViewById<TextView>(R.id.settingsButton)
        val dailyRewardsButton = findViewById<TextView>(R.id.dailyRewardsButton)
        val mapSelectionButton = findViewById<TextView>(R.id.mapSelectionButton)
        val leaderboardButton = findViewById<TextView>(R.id.leaderboardButton)
        val charactersButton = findViewById<TextView>(R.id.charactersButton)
        val analyticsButton = findViewById<TextView>(R.id.analyticsButton)

        // Mevcut leveli göster
        val currentLevel = LevelSystem.getCurrentLevel(this)
        val highestLevel = LevelSystem.getHighestLevel(this)
        val totalLevels = LevelSystem.getAllLevels().size

        currentLevelText.text = getString(R.string.current_level, currentLevel, highestLevel)

        // İlerleme durumunu güncelle
        progressText.text = getString(R.string.progress_text, highestLevel, totalLevels)
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

        // Settings butonu
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Daily Rewards butonu
        dailyRewardsButton.setOnClickListener {
            startActivity(Intent(this, DailyRewardsActivity::class.java))
        }

        // Map Selection butonu
        mapSelectionButton.setOnClickListener {
            startActivity(Intent(this, MapSelectionActivity::class.java))
        }

        // Leaderboard butonu
        leaderboardButton.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // Characters butonu
        charactersButton.setOnClickListener {
            startActivity(Intent(this, CharacterSelectionActivity::class.java))
        }

        // Analytics butonu
        analyticsButton.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
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
        title.text = getString(R.string.level_title, level)

        val descriptionResourceId = when (level) {
            1 -> R.string.level_1_desc
            2 -> R.string.level_2_desc
            3 -> R.string.level_3_desc
            4 -> R.string.level_4_desc
            5 -> R.string.level_5_desc
            6 -> R.string.level_6_desc
            7 -> R.string.level_7_desc
            8 -> R.string.level_8_desc
            9 -> R.string.level_9_desc
            10 -> R.string.level_10_desc
            else -> R.string.level_1_desc
        }

        desc.text = getString(descriptionResourceId)
    }

    private fun createLevelPath(container: LinearLayout) {
        val allLevels = LevelSystem.getAllLevels()
        val highestLevel = LevelSystem.getHighestLevel(this)

        for (i in allLevels.indices) {
            val level = allLevels[i]

            // FrameLayout (Button + Animasyon üst üste)
            val levelFrame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    120, // sabit boyut
                    120  // sabit boyut
                ).apply {
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
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                setOnClickListener {
                    LevelSystem.setCurrentLevel(context, level.levelNumber)
                    startActivity(Intent(context, MainActivity::class.java))
                }
            }

            // ✅ Tik animasyonu (Lottie) - tamamlanan leveller için
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

            // 🔗 Bağlayıcı animasyon - leveller arası
            if (i < allLevels.size - 1) {
                val connectorAnim = LottieAnimationView(this).apply {
                    setAnimation("question_linear.json")
                    repeatCount = LottieDrawable.INFINITE
                    layoutParams = LinearLayout.LayoutParams(
                        150,  // genişlik
                        150   // yükseklik
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

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", "tr") ?: "tr"
        setLocale(savedLanguage)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

}

