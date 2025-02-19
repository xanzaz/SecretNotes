package com.example.secretnotev02.customActivity

import androidx.appcompat.app.AppCompatActivity
import com.example.secretnotev02.ActivityCounter

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