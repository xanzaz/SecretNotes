package com.example.secretnotev02.DB

import com.example.secretnotev02.scripts.NoteExport
import java.io.Serializable

data class NoteTable (
    var id: Int,
    var title: String,
    var content: String,
    var date: String,
    var isActive: Boolean = false
): Serializable
{
    fun toNote():Note
    {
        return Note(id,title,content,date)
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