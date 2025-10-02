package com.erencanutku.arabaoyunu2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StartScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        // 💰 Para verisini göster
        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val money = prefs.getInt("money", 0)
        findViewById<TextView>(R.id.moneyText).text = "💰 $money"

        val btnGarage = findViewById<Button>(R.id.buttonGarage)
        val btnStart = findViewById<Button>(R.id.buttonStartGame)

        btnGarage.setOnClickListener {
            startActivity(Intent(this, GarageActivity::class.java))
        }

        btnStart.setOnClickListener {
            startActivity(Intent(this, MainMenuActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Para güncellemesini her dönüşte yap
        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)
        val money = prefs.getInt("money", 0)
        findViewById<TextView>(R.id.moneyText).text = "💰 $money"
    }
}
