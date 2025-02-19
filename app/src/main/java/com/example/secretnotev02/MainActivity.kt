package com.example.secretnotev02

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.secretnotes.scripts.Pref
import com.example.secretnotev02.customActivity.BaseActivity
import com.example.secretnotev02.fragments.LoginFragment
import com.example.secretnotev02.fragments.NotesFragment
import com.example.secretnotev02.fragments.RegistrationFragment
import com.example.secretnotev02.fragments.SecretNotesFragment
import com.example.secretnotev02.scripts.AppData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : BaseActivity() {

    lateinit var bottomNavView: BottomNavigationView
    private var isClose = false

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

    override fun onStart() {


        //Проверка было ли свернуто ли приложение в этом активити
        if (ActivityCounter.activityCount == 0)
            isClose = true

        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        //Если было свернуто приложение на этом окне и было открыто секретыне заметки овзвращаем к регистрации
        if (isClose)
        {
            val itemMenuNotes = intArrayOf(R.id.all_clear_selected_notes_menu,R.id.delete_selected_notes_menu,R.id.encrypt_selected_notes_menu)
            val itemMenuSecretNote = intArrayOf(R.id.all_clear_selected_secret_notes_menu,R.id.delete_selected_secret_notes_menu,R.id.import_selected_secret_notes_menu)
            val selectedItemId: Int = bottomNavView.getSelectedItemId()
            if (selectedItemId in itemMenuNotes)
            {
                onChangeMenu(R.menu.bottom_navigation_main)
                bottomNavView.setOnItemSelectedListener { item ->
                    listenerMain(item.itemId)
                }

            }
            else if ( selectedItemId in itemMenuSecretNote)
            {
                onChangeMenu(R.menu.bottom_navigation_main)
                bottomNavView.selectedItemId = R.id.nav_main_secret_notes
                bottomNavView.setOnItemSelectedListener { item ->
                    listenerMain(item.itemId)
                }
            }

            if (bottomNavView.selectedItemId == R.id.nav_main_secret_notes)
                loadFragment(LoginFragment())
            isClose = false
        }
    }






}