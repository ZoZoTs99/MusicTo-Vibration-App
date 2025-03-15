package com.example.musictovibrationapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.database.Cursor

class ActivityLibrary : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songsAdapter: SongsAdapter
    private val songs = mutableListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with click listener
        songsAdapter = SongsAdapter(this, songs) { selectedSong ->
            openPlayerMode(selectedSong)
        }
        recyclerView.adapter = songsAdapter

        showPermissionDialog()
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Access Music Library")
            .setMessage("This app needs access to your internal music library to display available songs.")
            .setPositiveButton("Allow") { dialog, _ ->
                dialog.dismiss()
                loadSongs()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Access denied. Unable to load songs.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    private fun loadSongs() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )

        songs.clear()
        cursor?.use {
            val idIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val pathIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val durationIndex = it.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val artist = it.getString(artistIndex) ?: "Unknown Artist"
                val title = it.getString(titleIndex) ?: "Unknown Title"
                val path = it.getString(pathIndex) ?: "Unknown Path"
                val duration = it.getInt(durationIndex)

                songs.add(Song(id, title, path, artist, duration))
            }
        }

        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE

        if (songs.isEmpty()) {
            findViewById<TextView>(R.id.emptyStateText).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.emptyStateText).visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            songsAdapter.notifyDataSetChanged()
        }
    }

    private fun openPlayerMode(song: Song) {
        val songTitles = ArrayList<String>()
        val songArtists = ArrayList<String>()
        val songListPaths = ArrayList<String>()
        var currentIndex = 0

        for (i in songs.indices) {
            songTitles.add(songs[i].title)
            songArtists.add(songs[i].artist)
            songListPaths.add(songs[i].path)
            if (songs[i].path == song.path) {
                currentIndex = i
            }
        }

        val intent = Intent(this, PlayerMode::class.java).apply {
            putStringArrayListExtra("SONG_TITLES", songTitles)
            putStringArrayListExtra("SONG_ARTISTS", songArtists)
            putStringArrayListExtra("SONG_LIST", songListPaths)
            putExtra("CURRENT_INDEX", currentIndex)
        }
        startActivity(intent)
    }
}
