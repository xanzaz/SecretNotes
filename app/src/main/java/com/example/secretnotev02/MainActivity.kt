package com.example.secretnotev02

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.secretnotes.scripts.Pref
import com.example.secretnotev02.fragments.LoginFragment
import com.example.secretnotev02.fragments.NotesFragment
import com.example.secretnotev02.fragments.RegistrationFragment
import com.example.secretnotev02.fragments.SecretNotesFragment
import com.example.secretnotev02.scripts.AppData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : BaseActivity() {

    lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadFragment(NotesFragment())

        ActivityCounter.onAppForegrounded =
            {
                if (bottomNavView.selectedItemId == R.id.nav_main_secret_notes)
                    loadFragment(LoginFragment())
            }

        //Кнопка переключения темы
        val radioButton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        radioButton.setOnClickListener {
            val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (isNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                androidx.cardview.R.style.CardView
            }
        }

        //Первый запуск
        val pref = Pref(this)
        if (pref.getValue("isFirst","false")=="false") {
            pref.saveValue("AES_Nb",4)
            pref.saveValue("AES_Nk",4)
            pref.saveValue("AES_Nr",10)
        }

        //нижнее меню
        bottomNavView = findViewById(R.id.bottomNavigationMain)
        bottomNavView.setOnItemSelectedListener { item ->
            listenerMain(item.itemId)
        }

        //Обработчик нажатия кнопки настройки
        val topMunu = findViewById<Toolbar>(R.id.toolbarMain)
        topMunu.setOnMenuItemClickListener { item ->
            when(item.itemId)
            {
                R.id.setting_toolbarMain ->
                {
                    startActivity(Intent(this,SettingLayoutActivity::class.java))
                    true
                }
                else -> true
            }
        }

    }
    //Метод запуска фрагментов
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.FrameLayautMain, fragment)
            .commit()
    }

    //Метод изменения меню
    fun onChangeMenu(menuResId: Int) {
        bottomNavView.menu.clear()
        bottomNavView.inflateMenu(menuResId)

    }

    //Обработчик кнопог лавного меню
    fun listenerMain(item: Int): Boolean {
        when(item)
        {
            R.id.nav_main_notes ->
            {
                loadFragment(NotesFragment())
                return  true
            }

            R.id.nav_main_secret_notes ->
            {
                if (AppData.isLogin)
                    loadFragment(SecretNotesFragment())
                else
                {
                    val prefHashPass = Pref(this).getValue("hashPass","")
                    if (prefHashPass == "")
                        loadFragment(RegistrationFragment())
                    else
                        loadFragment(LoginFragment())
                }
                return  true
            }
            else -> return  true
        }
    }

}