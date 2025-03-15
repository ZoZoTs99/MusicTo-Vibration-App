package com.example.musictovibrationapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import java.io.File


class PlayerMode : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var rhythmVibrator: RhythmVibrator
    private var songUri: String? = null

    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var songTitleTextView: TextView
    private lateinit var songArtistTextView: TextView
    private lateinit var visualizerView: VisualizerView

    private var songList: ArrayList<String>? = null  // List of songs
    private var songTitles = ArrayList<String>() // Song titles
    private var songArtists = ArrayList<String>() // Song artists
    private var currentIndex = 0 // Index of the current song
    private var isPlaying = true  // To track play/pause state

    // Use MainLooper for UI thread
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_mode)

        // Retrieve song data from intent
        songList = intent.getStringArrayListExtra("SONG_LIST")
        songTitles = intent.getStringArrayListExtra("SONG_TITLES") ?: arrayListOf()
        songArtists = intent.getStringArrayListExtra("SONG_ARTISTS") ?: arrayListOf()
        currentIndex = intent.getIntExtra("CURRENT_INDEX", 0)
        if (songList.isNullOrEmpty() || currentIndex !in songList!!.indices) {
            println("Error: Invalid song list or index!")
            finish()
            return
        }

        // Initialize UI elements
        songTitleTextView = findViewById(R.id.songTitle)
        songArtistTextView = findViewById(R.id.songArtist)
        startTimeTextView = findViewById(R.id.start_time)
        endTimeTextView = findViewById(R.id.end_time)
        seekBar = findViewById(R.id.seekBar)
        playPauseButton = findViewById(R.id.play_pause)
        nextButton = findViewById(R.id.next)
        prevButton = findViewById(R.id.previous)
        visualizerView = findViewById(R.id.VisualizerView)

        // Set up the back button functionality
        val backButton = findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener {
            finish()  // Closes PlayerMode and returns to ActivityLibrary
        }

        updateSongInfo()  // Update song title and artist
        updatePlayPauseButton()  // Set initial button state to play

        playPauseButton.setOnClickListener {
            isPlaying = !isPlaying  // Toggle play/pause state
            handlePlayPauseAction()
            updatePlayPauseButton()  // Update the button image based on state
        }

        // Set the listener to detect beats and trigger vibrations
        visualizerView.setOnBeatListener { intensity ->
            // Trigger vibration based on detected beat and frequency
            rhythmVibrator.vibrateToBeat(intensity)
        }




        nextButton.setOnClickListener { playNextSong() }
        prevButton.setOnClickListener { playPreviousSong() }

        rhythmVibrator = RhythmVibrator(this)


        playSong() // Play the initial song

        // Set SeekBar listener to update song position
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)  // Seek the song to the selected position
                    updateStartTimeTextView(progress)  // Update the start time to the new position
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)  // Stop updating time while the user is dragging the seek bar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    startUpdatingTime()  // Resume the time update once the user stops dragging the seek bar
                }
            }
        })

    }


    private fun playSong() {
        val safeSongList = songList ?: return  // Avoid null issues

        if (currentIndex !in safeSongList.indices) {
            println("Error: Invalid song index!")
            return
        }

        val uriString = safeSongList[currentIndex]
        songUri = uriString
        val uri = Uri.parse(uriString)

        // ✅ If mediaPlayer exists, reset it instead of creating a new one
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.reset()  // Instead of stopping/releasing, reset it for reuse
        } else {
            mediaPlayer = MediaPlayer()
        }

        // ✅ Setup the media player
        mediaPlayer.apply {
            setDataSource(applicationContext, uri)
            prepare()
            start()
            seekBar.max = duration
            updateEndTimeTextView(duration)
            updateStartTimeTextView(0)
            startUpdatingTime()
        }
        visualizerView.linkToMediaPlayer(mediaPlayer.audioSessionId)

        isPlaying = true  // ✅ Update play state
        updatePlayPauseButton()
    }




    private fun updateSongInfo() {
        songTitleTextView.text = songTitles[currentIndex] // Update song title
        songArtistTextView.text = songArtists[currentIndex] // Update artist name
    }

    private fun playNextSong() {
        songList?.let { list ->
            if (currentIndex < list.size - 1) {
                currentIndex++
                playSong()
                updateSongInfo() // Update UI after switching songs
            }
        }
    }

    private fun playPreviousSong() {
        songList?.let { list ->
            if (currentIndex > 0) {
                currentIndex--
                playSong()
                updateSongInfo() // Update UI after switching songs
            }
        }
    }



    private fun updatePlayPauseButton() {
        if (isPlaying) {
            playPauseButton.setImageResource(R.drawable.pause) // Set to pause icon
        } else {
            playPauseButton.setImageResource(R.drawable.play) // Set to play icon
        }
    }

    private fun handlePlayPauseAction() {
        if (!::mediaPlayer.isInitialized) return  // ✅ Prevent crashes if MediaPlayer is uninitialized

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            rhythmVibrator.stopVibration()
            mediaPlayer.setOnCompletionListener {
                visualizerView.release()  // Stop the visualizer when music finishes
            }
            handler.removeCallbacksAndMessages(null)  // Stop seek bar updates
        } else {
            mediaPlayer.start()
            isPlaying = true
            startUpdatingTime()
        }

        updatePlayPauseButton()  // ✅ Update UI
    }

    private fun updateStartTimeTextView(currentTime: Int) {
        val minutes = currentTime / 1000 / 60
        val seconds = currentTime / 1000 % 60
        startTimeTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateEndTimeTextView(duration: Int) {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        endTimeTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startUpdatingTime() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    updateStartTimeTextView(currentPosition) // Update the start time
                    seekBar.progress = currentPosition // Update the seek bar progress
                    handler.postDelayed(this, 1000) // Continue updating every second
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources when activity is destroyed
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        rhythmVibrator.stopVibration()
        visualizerView.release()
        handler.removeCallbacksAndMessages(null)// Remove all handler callbacks for UI updates
    }
}
