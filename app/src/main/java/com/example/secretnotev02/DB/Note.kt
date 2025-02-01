package com.example.secretnotev02.DB

import com.example.secretnotev02.scripts.NoteExport
import java.io.Serializable

data class Note (
    var id: Int?,
    val title: String,
    val content: String,
    val date: String,
): Serializable
{
    fun toNoteTable(): NoteTable
    {
        return NoteTable(id!!,title,content,date)
    }

    fun toNoteExport() : NoteExport
    {
        return NoteExport(
            title = title.trim(),
            content = content.trim(),
            date = date.trim()
        )
    }
}



