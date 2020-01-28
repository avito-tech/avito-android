package com.avito.android.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.cos
import kotlin.math.sin

class MovingButtonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moving_button)

        val deathZone = findViewById<View>(R.id.death_zone)
        val movingButton = findViewById<Button>(R.id.moving_button)
        val movingButtonClickedIndicator =
            findViewById<TextView>(R.id.moving_button_clicked_text_view)

        deathZone.setOnClickListener { throw RuntimeException("Clicked on a death zone") }

        thread {
            val fps = 30
            val iterationsCount = fps * 5 // 5 seconds (30 frames per every second)
            val radius = 50

            (0..iterationsCount)
                .forEach { iterationNumber ->
                    runOnUiThread {
                        val newX =
                            (movingButton.x + cos(iterationNumber.toDouble()) * radius).toFloat()
                        val newY =
                            (movingButton.y + sin(iterationNumber.toDouble()) * radius).toFloat()

                        movingButton.x = newX
                        movingButton.y = newY
                    }

                    try {
                        Thread.sleep((1000 / fps).toLong())
                    } catch (e: InterruptedException) {
                    }
                }
        }

        movingButton.setOnClickListener {
            movingButtonClickedIndicator.toggleVisibility()

            it.postDelayed(
                {
                    runOnUiThread {
                        movingButtonClickedIndicator.toggleVisibility()
                    }
                },
                TimeUnit.SECONDS.toMillis(3)
            )
        }
    }
}