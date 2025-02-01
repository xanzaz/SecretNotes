package com.example.secretnotev02.DB

import com.example.secretnotev02.scripts.AppData

class SecretNote (
    var id: Int?,
    var title: ByteArray,
    var content: ByteArray,
    var date: ByteArray,
)
{
    fun toNote(): Note
    {
        return Note(
            id = id,
            title = AppData.AES!!.decryptText(title),
            content = AppData.AES!!.decryptText(content),
            date = AppData.AES!!.decryptText(date)
        )
    }
}