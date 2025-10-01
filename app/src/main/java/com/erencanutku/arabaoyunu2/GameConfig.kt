package com.erencanutku.arabaoyunu2

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

object GameConfig {
    const val INITIAL_LIVES = 99
    const val CAR_SPEED = 5f
    const val COLLISION_THRESHOLD = 100
    const val MAX_QUESTIONS_PER_LEVEL = 5
    const val MAX_TOTAL_QUESTIONS = 20
    const val CAR_MOVE_SPEED = 3f
}

class CarController(
    private val carImage: ImageView,
    private val rootLayout: ConstraintLayout,
    private val gameManager: GameManager,
    private val onBlockedMove: () -> Unit
) {
    private var blockedAttempts = 0
    private val moveDistance = 150f

    fun moveLeft() {
        val nextX = carImage.translationX - moveDistance
        if (isWithinBounds(nextX)) {
            animateMove(nextX)
        } else {
            handleBlockedMove()
        }
    }

    fun moveRight() {
        val nextX = carImage.translationX + moveDistance
        if (isWithinBounds(nextX)) {
            animateMove(nextX)
        } else {
            handleBlockedMove()
        }
    }

    private fun animateMove(targetX: Float) {
        ObjectAnimator.ofFloat(carImage, "translationX", targetX).apply {
            duration = 250
            start()
        }
    }

    private fun isWithinBounds(x: Float): Boolean {
        val carWidth = carImage.width
        val screenWidth = rootLayout.width
        val leftBound = -screenWidth / 2f + carWidth / 2f
        val rightBound = screenWidth / 2f - carWidth / 2f
        return x in leftBound..rightBound
    }

    private fun handleBlockedMove() {
        blockedAttempts++
        onBlockedMove()

        val shake = TranslateAnimation(0f, 20f, 0f, 0f).apply {
            duration = 100
            repeatMode = Animation.REVERSE
            repeatCount = 2
        }
        carImage.startAnimation(shake)

        if (blockedAttempts >= 2) {
            blockedAttempts = 0
            val lost = gameManager.loseFuel(10f)
            Toast.makeText(
                carImage.context,
                "💥 Kenara çarptın! -10% Yakıt",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun resetPosition() {
        carImage.translationX = 0f
        carImage.translationY = 600f
        blockedAttempts = 0
    }
}







