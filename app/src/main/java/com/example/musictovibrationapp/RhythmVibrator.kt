package com.example.musictovibrationapp

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class RhythmVibrator(private val context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrateToBeat(intensity: Int) {
        val duration = mapIntensityToDuration(intensity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+ (Android 8.0+)
            val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            // Older versions (API 24 and below)
            vibrator.vibrate(duration)
        }
    }

    private fun mapIntensityToDuration(intensity: Int): Long {
        return when {
            intensity > 80 -> 255 // Strong beat
            intensity > 50 -> 150 // Medium beat
            else -> 50 // Light beat
        }
    }

    fun stopVibration() {
        vibrator.cancel()
    }
}