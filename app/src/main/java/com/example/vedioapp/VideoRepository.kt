package com.example.vedioapp

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(private val application: Application) {

    suspend fun compressAndSaveVideo(uri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val resolver = application.contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return@withContext null
            val outputDir = application.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            val outputFile = File(outputDir, "compressed_video_${System.currentTimeMillis()}.mp4")

            // Save input video to temporary file
            val tempInputFile = File(outputDir, "temp_video_${System.currentTimeMillis()}.mp4")
            Log.d("VideoRepository", "Saving input video to temporary file: ${tempInputFile.absolutePath}")
            resolver.openOutputStream(Uri.fromFile(tempInputFile))?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            // Compress video using FFmpeg
            val command = "-i ${tempInputFile.absolutePath} -vcodec libx264 -crf 28 ${outputFile.absolutePath}"
            Log.d("VideoRepository", "Executing FFmpeg command: $command")
            val session = FFmpegKit.execute(command)

            if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d("VideoRepository", "Video compression successful, saving to MediaStore")
                // Save the compressed video to MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                }

                val videoCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val newUri = resolver.insert(videoCollection, contentValues)

                newUri?.let { newUri ->
                    resolver.openOutputStream(newUri)?.use { outputStream ->
                        outputFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                // Clean up temporary files
                tempInputFile.delete()
                outputFile.delete()

                newUri
            } else {
                Log.e("VideoRepository", "Video compression failed: ${session.failStackTrace}")
                tempInputFile.delete()
                null
            }
        }
    }
}