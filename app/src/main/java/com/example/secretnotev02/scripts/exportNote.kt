package com.example.secretnotev02.scripts


import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


import androidx.core.content.ContextCompat
import com.example.secretnotev02.DB.Note
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

fun exportSecretNotes(context: Context, fileName: String, notes: List<Note>): Uri?
{
    if (notes.isEmpty())
    {
        Toast.makeText(context,"У вас нет секретных заметок.",Toast.LENGTH_SHORT).show()
        return null
    }
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
        Toast.makeText(context,"Данные успешно сохранены и находятся в папке Documents.",Toast.LENGTH_SHORT).show()
        uri
    }
    catch (e: Exception)
    {
        Toast.makeText(context,"Произошла ошибка при сохранении данных.",Toast.LENGTH_SHORT).show()
        Log.e("exportSecretNotes", e.message.toString())
        null
    }



}

