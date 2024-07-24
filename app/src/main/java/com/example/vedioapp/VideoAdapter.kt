package com.example.vedioapp

import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vedioapp.R

class VideoAdapter(private val onVideoClick: (Uri) -> Unit) : ListAdapter<Uri, VideoAdapter.VideoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view, onVideoClick)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VideoViewHolder(itemView: View, private val onVideoClick: (Uri) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val videoTitle: TextView = itemView.findViewById(R.id.video_title)

        fun bind(uri: Uri) {
            val context = itemView.context
            val contentResolver = context.contentResolver

            val projection = arrayOf(MediaStore.Video.Media.DISPLAY_NAME)
            val cursor = contentResolver.query(uri, projection, null, null, null)
            val nameIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            cursor?.moveToFirst()
            val videoName = nameIndex?.let { cursor.getString(it) } ?: "Unknown"

            videoTitle.text = videoName
            cursor?.close()

            itemView.setOnClickListener {
                onVideoClick(uri)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
    }
}
