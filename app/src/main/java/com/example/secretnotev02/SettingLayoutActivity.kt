package com.example.secretnotev02

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.secretnotev02.databinding.ActivitySettingLayoutBinding
import com.example.secretnotev02.fragments.SettingListFragment

class SettingLayoutActivity : BaseActivity() {
    lateinit var bilding: ActivitySettingLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bilding = ActivitySettingLayoutBinding.inflate(layoutInflater)
        setContentView(bilding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadFragment(SettingListFragment())




    }
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.FrameSettingLayout, fragment)
            .commit()
    }
}