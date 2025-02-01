package com.example.secretnotev02.DB

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//Класс для работы с таблицами в БД
class DbHelper (val context: Context, val factory: CursorFactory?)
    : SQLiteOpenHelper(context,"app", factory, 1){
    override fun onCreate(db: SQLiteDatabase?) {
        var query = "CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT,date TEXT)"
        db!!.execSQL(query)
        query = "CREATE TABLE secret_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title BLOB, content BLOB,date BLOB)"
        db.execSQL(query)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS notes")
        db.execSQL("DROP TABLE IF EXISTS secret_notes")
        onCreate(db)

    }

    // Note

    //Получение всех заметок
    fun allNotes(): List<Note> {
        val db = this.readableDatabase
        var notes = mutableListOf<Note>()
        val result = db.rawQuery("SELECT * FROM notes",null)

        with(result)
        {
            while (moveToNext())
            {
                notes.add(Note(
                    id = getInt(getColumnIndexOrThrow("id")),
                    title = getString(getColumnIndexOrThrow("title")),
                    content = getString(getColumnIndexOrThrow("content")),
                    date = getString(getColumnIndexOrThrow("date"))
                ))
            }
        }

        db.close()
        return notes
    }

    //Создание новой заметки в БД
    fun addNote(note: Note) : Int{
        val value = ContentValues()
        value.put("title", note.title)
        value.put("content",note.content)
        value.put("date",note.date)

        val db = this.writableDatabase
        val id = db.insert("notes",null, value)
        db.close()
        return id.toInt()
    }

    //Обновление данных заметки
    fun updateNotes(note: Note) {
        Log.i("DbHelper", "updateNotes: Note(id=${note.id} title=${note.title} content=${note.content} date=${note.date} )")
        val value = ContentValues()
        value.put("title", note.title)
        value.put("content",note.content)
        value.put("date",note.date)

        val whereClause = "id = ?"
        val whereArgs = arrayOf(note.id.toString())

        val db = this.writableDatabase
        db.update("notes", value, whereClause, whereArgs)
        db.close()
    }

    //Удаление заметки
    fun deleteNote(id: Int) {
        val db = this.writableDatabase

        val whereClause = "id = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete("notes",whereClause,whereArgs)
    }




    //Secret note

    //Получение всех секретных заметок
    fun allSecretNotes(): MutableList<SecretNote> {
        val db = this.readableDatabase
        var secretNotes = mutableListOf<SecretNote>()
        val result = db.rawQuery("SELECT * FROM secret_notes",null)

        with(result)
        {
            while (moveToNext())
            {
                secretNotes.add(
                    SecretNote(
                        id = getInt(getColumnIndexOrThrow("id")),
                        title = getBlob(getColumnIndexOrThrow("title")),
                        content = getBlob(getColumnIndexOrThrow("content")),
                        date = getBlob(getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        db.close()
        return secretNotes
    }

    //Добавление секретной заметки
    fun addSecretNote(secretNote: SecretNote): Int {
        val value = ContentValues()
        value.put("title", secretNote.title)
        value.put("content",secretNote.content)
        value.put("date",secretNote.date)

        val db = this.writableDatabase
        val id = db.insert("secret_notes",null,value)
        db.close()
        return id.toInt()
    }

    //Обновление данных секретной заметки
    fun updateSecretNotes(secretNote: SecretNote) {
        val value = ContentValues()
        value.put("title", secretNote.title)
        value.put("content",secretNote.content)
        value.put("date",secretNote.date)

        val whereClause = "id = ?"
        val whereArgs = arrayOf(secretNote.id.toString())

        val db = this.writableDatabase
        db.update("secret_notes", value, whereClause, whereArgs)
        db.close()
    }

    //Удаление секретной заметки
    fun deleteSecretNote(id: Int) {
        val db = this.writableDatabase

        val whereClause = "id = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete("secret_notes",whereClause,whereArgs)
    }

}
