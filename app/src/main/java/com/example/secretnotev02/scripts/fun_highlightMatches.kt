package com.example.secretnotev02.scripts

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import java.util.Locale

fun highlightMatches(text: String, query: String?): SpannableString {
    val spannable = SpannableString(text)
    if (query.isNullOrEmpty()) return spannable

    val lowerText = text.lowercase(Locale.getDefault())
    val lowerQuery = query.lowercase(Locale.getDefault())

    var startIndex = 0
    while (true) {
        val index = lowerText.indexOf(lowerQuery, startIndex)
        if (index == -1) break // Выход, если совпадений больше нет

        // Устанавливаем цвет для совпадения
        spannable.setSpan(
            ForegroundColorSpan(Color.RED), // Красный цвет
            index,
            index + query.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        startIndex = index + query.length
    }
    return spannable
}