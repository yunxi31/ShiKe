package com.pomodoroalert.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object RingtoneCopyHelper {
    fun copyRingtoneToInternal(context: Context, uri: Uri, alarmId: String): String? {
        android.util.Log.d("RingtoneCopy", "copyRingtoneToInternal called with uri: $uri, alarmId: $alarmId")
        return try {
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use { inputStream ->
                val dir = File(context.filesDir, "ringtones")
                if (!dir.exists()) {
                    val created = dir.mkdirs()
                    android.util.Log.d("RingtoneCopy", "ringtones dir created: $created")
                }
                val targetFile = File(dir, "ringtone_$alarmId.mp3")
                android.util.Log.d("RingtoneCopy", "targetFile path: ${targetFile.absolutePath}")
                FileOutputStream(targetFile).use { outputStream ->
                    val bytesCopied = inputStream.copyTo(outputStream)
                    android.util.Log.d("RingtoneCopy", "bytesCopied: $bytesCopied")
                }
                val resultUri = Uri.fromFile(targetFile).toString()
                android.util.Log.d("RingtoneCopy", "copyRingtoneToInternal success, resultUri: $resultUri")
                resultUri
            }
        } catch (e: Exception) {
            android.util.Log.e("RingtoneCopy", "copyRingtoneToInternal failed", e)
            null
        }
    }

    fun getRingtoneDisplayName(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex)
                        }
                        if (name.isNullOrBlank()) {
                            val titleIndex = it.getColumnIndex("title")
                            if (titleIndex != -1) {
                                name = it.getString(titleIndex)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment
        }
        return name ?: "自定义铃声"
    }
}
