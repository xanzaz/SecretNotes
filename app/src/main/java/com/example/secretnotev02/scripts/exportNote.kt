package com.example.secretnotev02.scripts


import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.secretnotev02.DB.NoteTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import java.io.File
import java.io.FileOutputStream

@Serializable
data class NoteExport(
    val title: String,
    val content: String,
    val date: String
)

fun exportSecretNotes(context: Context, fileName: String, notes: List<NoteTable>): Uri?
{
    val notesEport = notes.map { it.toNoteExport() }
    val strJson = Json.encodeToString(notesEport)
    val data = AppData.AES!!.encodingText(strJson)

    return try {

        // Создаем объект ContentValues для метаданных файла
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) // Имя файла
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream") // MIME-тип
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MyApp") // Путь
        }

        // Получаем ContentResolver
        val resolver = context.contentResolver

        // Вставляем запись в MediaStore и получаем URI файла
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        // Записываем данные в файл
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(data)
            }
        }

        uri
    }
    catch (e: Exception)
    {
        Log.e("exportSecretNotes", e.message.toString())
        null
    }



}

