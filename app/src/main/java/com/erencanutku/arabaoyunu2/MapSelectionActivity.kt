package com.erencanutku.arabaoyunu2

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class MapSelectionActivity : AppCompatActivity() {

    private lateinit var backButton: TextView
    private lateinit var mapsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)

        initializeViews()
        setupMaps()
        setupListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        mapsContainer = findViewById(R.id.mapsContainer)
    }

    private fun setupMaps() {
        val allMaps = MapSystem.getAllMaps()
        val selectedMap = MapSystem.getSelectedMap(this)

        mapsContainer.removeAllViews()

        for (mapTheme in allMaps) {
            val cardView = createMapCard(mapTheme, selectedMap.id == mapTheme.id)
            mapsContainer.addView(cardView)
        }
    }

    private fun createMapCard(mapTheme: MapTheme, isSelected: Boolean): CardView {
        val isUnlocked = MapSystem.isMapUnlocked(this, mapTheme)

        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 16f
            cardElevation = 12f

            // Kartın durumuna göre renk
            when {
                isSelected -> setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
                isUnlocked -> setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                else -> setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }

            // Tıklama efekti
            if (isUnlocked) {
                isClickable = true
                isFocusable = true
                foreground = ContextCompat.getDrawable(context, android.R.drawable.btn_default)

                setOnClickListener {
                    selectMap(mapTheme)
                }
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
        }

        // Harita önizlemesi
        val previewLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(120, 80)
            setBackgroundResource(mapTheme.backgroundResource)
        }

        // Harita bilgileri
        val infoLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 0, 0)
        }

        val nameText = TextView(this).apply {
            text = mapTheme.name
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val descText = TextView(this).apply {
            text = mapTheme.description
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }

        val statusText = TextView(this).apply {
            text = when {
                isSelected -> "✅ Seçili"
                isUnlocked -> "🔓 Açık (Level ${mapTheme.unlockedAtLevel}+)"
                else -> "🔒 Kilitli (Level ${mapTheme.unlockedAtLevel} gerekli)"
            }
            textSize = 12f
            setTextColor(
                if (isUnlocked) ContextCompat.getColor(context, android.R.color.white)
                else ContextCompat.getColor(context, android.R.color.darker_gray)
            )
        }

        infoLayout.addView(nameText)
        infoLayout.addView(descText)
        infoLayout.addView(statusText)

        layout.addView(previewLayout)
        layout.addView(infoLayout)
        cardView.addView(layout)

        return cardView
    }

    private fun selectMap(mapTheme: MapTheme) {
        if (!MapSystem.isMapUnlocked(this, mapTheme)) {
            Toast.makeText(this, "Bu harita henüz kilitli!", Toast.LENGTH_SHORT).show()
            return
        }

        MapSystem.setSelectedMap(this, mapTheme.id)
        Toast.makeText(this, "${mapTheme.name} seçildi!", Toast.LENGTH_SHORT).show()

        // UI'ı güncelle
        setupMaps()
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }
}