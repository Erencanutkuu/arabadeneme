package com.erencanutku.arabaoyunu2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GarageActivity : AppCompatActivity() {

    private lateinit var car1: ImageView
    private lateinit var car2: ImageView
    private lateinit var car3: ImageView

    private lateinit var lock1: ImageView
    private lateinit var lock2: ImageView
    private lateinit var lock3: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garage)

        val prefs = getSharedPreferences("GameConfig", Context.MODE_PRIVATE)

        // Arabaları ve kilit ikonlarını bul
        car1 = findViewById(R.id.carOption1)
        car2 = findViewById(R.id.carOption2)
        car3 = findViewById(R.id.carOption3)

        lock1 = findViewById(R.id.lockIcon1)
        lock2 = findViewById(R.id.lockIcon2)
        lock3 = findViewById(R.id.lockIcon3)

        setupCar(prefs, "car1", car1, lock1)
        setupCar(prefs, "car2", car2, lock2)
        setupCar(prefs, "car3", car3, lock3)
    }

    private fun setupCar(prefs: android.content.SharedPreferences, carId: String, carImage: ImageView, lockIcon: ImageView) {
        val owned = prefs.getBoolean("ownedCar_$carId", carId == "car1") // car1 hep açık

        if (owned) {
            lockIcon.visibility = View.GONE
            carImage.setOnClickListener {
                prefs.edit().putString("selectedCar", carId).apply()
                Toast.makeText(this, "🚗 $carId seçildi!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            lockIcon.visibility = View.VISIBLE
            lockIcon.setOnClickListener {
                val currentMoney = prefs.getInt("money", 0)

                if (currentMoney >= 500) {
                    AlertDialog.Builder(this)
                        .setTitle("Satın Al")
                        .setMessage("Bu arabayı 500 paraya satın almak istiyor musun?")
                        .setPositiveButton("Evet") { _, _ ->
                            prefs.edit()
                                .putBoolean("ownedCar_$carId", true)
                                .putInt("money", currentMoney - 500)
                                .apply()
                            Toast.makeText(this, "$carId satın alındı!", Toast.LENGTH_SHORT).show()
                            recreate()
                        }
                        .setNegativeButton("Hayır", null)
                        .show()
                } else {
                    Toast.makeText(this, "Yetersiz para! 💸", Toast.LENGTH_SHORT).show()
                }
            }

            // Resme tıklama çalışmasın (kilitliyse)
            carImage.setOnClickListener {
                Toast.makeText(this, "Bu araba kilitli 🔒", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
