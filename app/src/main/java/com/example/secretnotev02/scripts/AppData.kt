package com.example.secretnotev02.scripts

import com.example.secretnotes.scripts.AES.AES

//обьект для хранения временных данных
object AppData {
    var AES: AES? = null
    var isLogin = false
}