package com.erencanutku.arabaoyunu2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private lateinit var splashLottie: LottieAnimationView
    private lateinit var progressBar: ProgressBar
    private lateinit var tipText: TextView
    private lateinit var loadingText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    private val tips = listOf(
        "💡 Doğru cevaplar yakıt verir!",
        "⚡ Yanlış cevaplar yakıt tüketir!",
        "🏁 Seviyeleri tamamlayarak yeni haritalar açın!",
        "🎁 Her gün giriş yaparak bonus coin kazanın!",
        "🚗 Garajdan farklı arabalar seçebilirsiniz!",
        "📳 Ayarlardan titreşimi kapatabilirsiniz!",
        "🗺️ 4 farklı harita temasını keşfedin!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeViews()
        startLoading()
    }

    private fun initializeViews() {
        splashLottie = findViewById(R.id.splashLottie)
        progressBar = findViewById(R.id.progressBar)
        tipText = findViewById(R.id.tipText)
        loadingText = findViewById(R.id.loadingText)

        // Random tip göster
        tipText.text = tips.random()

        // Lottie animasyonu başlat
        splashLottie.playAnimation()
    }

    private fun startLoading() {
        val loadingRunnable = object : Runnable {
            override fun run() {
                progress += 2

                // Progress bar'ı güncelle
                progressBar.progress = progress

                // Loading text'i güncelle
                loadingText.text = when {
                    progress <= 30 -> "🔧 Oyun yükleniyor..."
                    progress <= 60 -> "🗺️ Haritalar hazırlanıyor..."
                    progress <= 90 -> "🚗 Arabalar başlatılıyor..."
                    else -> "✅ Hazır!"
                }

                if (progress >= 100) {
                    // Yükleme tamamlandı, StartScreenActivity'ye geç
                    handler.postDelayed({
                        startActivity(Intent(this@SplashActivity, StartScreenActivity::class.java))
                        finish()
                    }, 500)
                } else {
                    handler.postDelayed(this, 50) // 50ms'de bir güncelle
                }
            }
        }

        handler.postDelayed(loadingRunnable, 1000) // 1 saniye bekle sonra başla
    }

    override fun onBackPressed() {
        // Loading sırasında geri tuşunu devre dışı bırak
    }
}