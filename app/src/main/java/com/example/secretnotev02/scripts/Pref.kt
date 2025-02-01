package com.example.secretnotes.scripts

import android.content.Context
import android.content.SharedPreferences

// класс для взаимодействие с внутренней памятью приложения
class Pref(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)

    // Сохранение значения
    fun saveValue(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun saveValue(key: String, value: Int)
    {
        sharedPreferences.edit().putInt(key,value).apply()
    }

    // Получение значения
    fun getValue(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    fun getValue(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue) ?: defaultValue
    }
}