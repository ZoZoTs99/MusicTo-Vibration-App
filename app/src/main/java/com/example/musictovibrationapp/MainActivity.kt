package com.example.musictovibrationapp

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var soundWaveAnimationView: ImageView
    private lateinit var rootLayout: RelativeLayout  // Use the root layout instead of left and right halves

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        soundWaveAnimationView = findViewById(R.id.soundWaveAnimation)

        Glide.with(this)
            .load(R.drawable.sound_wave_animation) // Replace with your image URL or resource
            .into(soundWaveAnimationView)

        // Find the root layout, which will cover the entire screen
        rootLayout = findViewById(R.id.rootLayout)

        // Set up gesture detector
        gestureDetector = GestureDetector(this, SwipeGestureListener())

        // Set touch listener on the root layout (covers both halves)
        rootLayout.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    // Gesture listener to detect swipe
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null && e2 != null) {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                // Only check for horizontal swipes
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        // Trigger navigation to the desired activity
                        navigateToActivity()  // This will trigger the navigation regardless of direction
                        return true
                    }
                }
            }
            return false
        }
    }

    // Function to navigate to one of the activities
    private fun navigateToActivity() {
        // You can navigate to either activity here, depending on your needs
        // For example, navigating to ActivityLibrary:
        val intent = Intent(this, ActivityLibrary::class.java)
        startActivity(intent)
    }
}
