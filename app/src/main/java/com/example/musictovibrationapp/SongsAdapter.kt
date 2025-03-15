package com.example.musictovibrationapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Song(
    val id: Long,
    val title: String,
    val path: String,
    val artist: String,
    val duration: Int // Duration in milliseconds
) {
    fun getFormattedDuration(): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

class SongsAdapter(
    private val context: Context,
    private val songs: List<Song>,
    private val onItemClick: (Song) -> Unit // Lambda for handling item clicks
) : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item_layout, parent, false)
        return SongsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val song = songs[position]

        // Set the song title, artist, and formatted duration
        holder.songTitle.text = song.title
        holder.artistName.text = song.artist
        holder.songDuration.text = song.getFormattedDuration()

        // Trigger the click listener with the selected song
        holder.itemView.setOnClickListener {
            onItemClick(song) // Pass the clicked song to the lambda
        }
    }

    override fun getItemCount(): Int = songs.size

    // ViewHolder class to hold views for each song item
    inner class SongsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
        val songDuration: TextView = itemView.findViewById(R.id.songDuration)
    }
}