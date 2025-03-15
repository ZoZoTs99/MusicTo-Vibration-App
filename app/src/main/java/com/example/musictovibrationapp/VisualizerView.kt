package com.example.musictovibrationapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.View
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.log2

class VisualizerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var visualizer: Visualizer? = null
    private val paint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL

    }
    private var fftBytes: ByteArray? = null
    private var onBeatDetected: ((Int) -> Unit)? = null // Callback for beats

    fun setOnBeatListener(listener: (Int) -> Unit) {
        this.onBeatDetected = listener
    }

    fun linkToMediaPlayer(mediaPlayerSessionId: Int) {
        release()
        visualizer = Visualizer(mediaPlayerSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

                override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    fftBytes = fft
                    detectBeats(fft)
                    invalidate()
                }
            }, Visualizer.getMaxCaptureRate(), false, true)
            enabled = true
        }
    }

    private fun detectBeats(fft: ByteArray?) {
        fft?.let { bytes ->
            val bassFrequencyIndex = 5 // Lower frequencies (bass)
            val real = bytes[bassFrequencyIndex * 2].toFloat()
            val imaginary = bytes[bassFrequencyIndex * 2 + 1].toFloat()
            val magnitude = kotlin.math.sqrt(real * real + imaginary * imaginary)

            val beatThreshold = 30 // Adjust this threshold based on intensity
            if (magnitude > beatThreshold) {
                onBeatDetected?.invoke(magnitude.toInt()) // Send beat intensity
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        fftBytes?.let { bytes ->
            val barCount = min(64, bytes.size / 2)
            val barWidth = width / barCount.toFloat()

            for (i in 0 until barCount) {
                val real = bytes[i * 2].toFloat()
                val imaginary = bytes[i * 2 + 1].toFloat()
                val magnitude = kotlin.math.sqrt(real * real + imaginary * imaginary)

                val scaledIndex = kotlin.math.log10((i + 1).toDouble()).toFloat()
                val barHeight = (magnitude / 256) * height * scaledIndex

                canvas.drawRect(
                    i * barWidth,
                    height - barHeight,
                    (i + 1) * barWidth,
                    height.toFloat(),
                    paint
                )
            }
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }
}
