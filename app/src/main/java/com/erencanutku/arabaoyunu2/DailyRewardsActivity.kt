package com.erencanutku.arabaoyunu2

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class DailyRewardsActivity : AppCompatActivity() {

    private lateinit var backButton: TextView
    private lateinit var claimButton: Button
    private lateinit var streakText: TextView
    private lateinit var rewardsContainer: LinearLayout

    private val dailyRewards = listOf(50, 75, 100, 150, 200, 300, 500) // 7 günlük ödüller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_rewards)

        initializeViews()
        setupRewards()
        setupListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        claimButton = findViewById(R.id.claimButton)
        streakText = findViewById(R.id.streakText)
        rewardsContainer = findViewById(R.id.rewardsContainer)
    }

    private fun setupRewards() {
        val prefs = getSharedPreferences("DailyRewards", Context.MODE_PRIVATE)
        val currentStreak = getCurrentStreak()
        val canClaim = canClaimToday()

        streakText.text = "🔥 Streak: $currentStreak gün"
        claimButton.isEnabled = canClaim

        if (canClaim) {
            claimButton.text = "🎁 Günlük Ödülü Al (${dailyRewards[currentStreak % 7]} coin)"
        } else {
            claimButton.text = "✅ Bugün Alındı"
        }

        // Ödül kartlarını oluştur
        createRewardCards(currentStreak)
    }

    private fun createRewardCards(currentStreak: Int) {
        rewardsContainer.removeAllViews()

        for (i in dailyRewards.indices) {
            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                radius = 12f
                cardElevation = 8f

                // Kartın durumuna göre renk
                when {
                    i < currentStreak % 7 -> setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
                    i == currentStreak % 7 && canClaimToday() -> setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                    else -> setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                }
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setPadding(16, 24, 16, 24)
            }

            val dayText = TextView(this).apply {
                text = "Gün ${i + 1}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = android.view.Gravity.CENTER
            }

            val rewardText = TextView(this).apply {
                text = "${dailyRewards[i]}\n💰"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            layout.addView(dayText)
            layout.addView(rewardText)
            cardView.addView(layout)
            rewardsContainer.addView(cardView)
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        claimButton.setOnClickListener {
            claimDailyReward()
        }
    }

    private fun claimDailyReward() {
        if (!canClaimToday()) {
            Toast.makeText(this, "Bugün zaten aldın!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentStreak = getCurrentStreak()
        val reward = dailyRewards[currentStreak % 7]

        // Ödülü ver
        val gamePrefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val currentMoney = gamePrefs.getInt("money", 0)
        gamePrefs.edit().putInt("money", currentMoney + reward).apply()

        // Streak'i güncelle
        val rewardPrefs = getSharedPreferences("DailyRewards", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        rewardPrefs.edit()
            .putString("last_claim_date", today)
            .putInt("streak", currentStreak + 1)
            .apply()

        Toast.makeText(this, "🎉 $reward coin kazandın!", Toast.LENGTH_LONG).show()

        // UI'ı güncelle
        setupRewards()
    }

    private fun getCurrentStreak(): Int {
        val prefs = getSharedPreferences("DailyRewards", Context.MODE_PRIVATE)
        val lastClaimDate = prefs.getString("last_claim_date", "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        return when (lastClaimDate) {
            today -> prefs.getInt("streak", 0) // Bugün aldıysa mevcut streak
            yesterday -> prefs.getInt("streak", 0) // Dün aldıysa streak devam ediyor
            else -> 0 // Streak kopmuş
        }
    }

    private fun canClaimToday(): Boolean {
        val prefs = getSharedPreferences("DailyRewards", Context.MODE_PRIVATE)
        val lastClaimDate = prefs.getString("last_claim_date", "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return lastClaimDate != today
    }
}