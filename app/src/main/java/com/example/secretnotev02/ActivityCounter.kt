package com.example.secretnotev02

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.secretnotev02.scripts.AppData

object ActivityCounter {
    var activityCount = 0
    var wasInBackground = true
    var isOpenSelectedFile = false

    fun activityStarted() {
        if(!isOpenSelectedFile)
        {
            if (activityCount == 0 && wasInBackground) {
                // Приложение вернулось на передний план

                onAppForegrounded?.invoke()
            }
            activityCount++
            wasInBackground = false
        }
    }

    fun activityStopped() {
        if(!isOpenSelectedFile) {
            activityCount--
            if (activityCount == 0) {
                // Все активности остановились
                Log.d("ActivityCounter", "activityStopped очищение данных")
                AppData.isLogin = false
                AppData.AES = null

                wasInBackground = true
                onAppBackgrounded?.invoke()
            }
        }
    }

    // Коллбэки
    var onAppBackgrounded: (() -> Unit)? = null
    var onAppForegrounded: (() -> Unit)? = null
}



