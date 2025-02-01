package com.example.secretnotev02

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.secretnotev02.scripts.AppData

object ActivityCounter {
    var activityCount = 0
    var wasInBackground = true

    fun activityStarted() {
        if (activityCount == 0 && wasInBackground) {
            // Приложение вернулось на передний план

            onAppForegrounded?.invoke()
        }
        activityCount++
        wasInBackground = false
    }

    fun activityStopped() {
        activityCount--
        if (activityCount == 0) {
            // Все активности остановились
            AppData.isLogin=false
            AppData.AES = null

            wasInBackground = true
            onAppBackgrounded?.invoke()
        }
    }

    // Коллбэки
    var onAppBackgrounded: (() -> Unit)? = null
    var onAppForegrounded: (() -> Unit)? = null
}

abstract class BaseActivity:  AppCompatActivity()
{
    override fun onStart() {
        super.onStart()
        ActivityCounter.activityStarted()
    }

    override fun onStop() {
        super.onStop()
        ActivityCounter.activityStopped()
    }

}
