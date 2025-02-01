package com.example.secretnotev02.DB

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
}



