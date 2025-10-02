package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class LevelCompletionActivity : AppCompatActivity() {

    private lateinit var completionLottie: LottieAnimationView
    private lateinit var progressBar: ProgressBar
    private lateinit var levelText: TextView
    private lateinit var loadingText: TextView
    private lateinit var rewardText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_completion)

        initializeViews()
        showLevelCompletion()
    }

    private fun initializeViews() {
        completionLottie = findViewById(R.id.completionLottie)
        progressBar = findViewById(R.id.progressBar)
        levelText = findViewById(R.id.levelText)
        loadingText = findViewById(R.id.loadingText)
        rewardText = findViewById(R.id.rewardText)

        // Tamamlanan level bilgisini göster
        val completedLevel = LevelSystem.getCurrentLevel(this) - 1
        levelText.text = "🏆 $completedLevel. Seviye Tamamlandı!"
        rewardText.text = "💰 +50 Coin Kazandınız!"

        // Success animasyonu başlat
        completionLottie.playAnimation()
    }

    private fun showLevelCompletion() {
        val loadingRunnable = object : Runnable {
            override fun run() {
                progress += 3

                // Progress bar'ı güncelle
                progressBar.progress = progress

                // Loading text'i güncelle
                loadingText.text = when {
                    progress <= 25 -> "🎉 Başarı kaydediliyor..."
                    progress <= 50 -> "💰 Ödüller hesaplanıyor..."
                    progress <= 75 -> "🔓 Yeni seviye açılıyor..."
                    else -> "✅ Hazır!"
                }

                if (progress >= 100) {
                    // Yükleme tamamlandı, MainMenuActivity'ye dön
                    handler.postDelayed({
                        startActivity(Intent(this@LevelCompletionActivity, MainMenuActivity::class.java))
                        finish()
                    }, 800)
                } else {
                    handler.postDelayed(this, 60) // 60ms'de bir güncelle
                }
            }
        }

        handler.postDelayed(loadingRunnable, 1000) // 1 saniye bekle sonra başla
    }

    override fun onBackPressed() {
        // Loading sırasında geri tuşunu devre dışı bırak
    }
}