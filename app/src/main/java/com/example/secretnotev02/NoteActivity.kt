package com.example.secretnotev02

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.customActivity.CustomNoteActivity
import com.example.secretnotev02.databinding.ActivityNoteBinding
import com.example.secretnotev02.scripts.highlightMatches
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class NoteActivity : CustomNoteActivity() {

    private lateinit var binding: ActivityNoteBinding
//    private lateinit var menu: Menu
    private var searchQuery: String? = null

//    private var resultCode: Int = RESULT_CANCELED
//    private lateinit var intent_out: Intent

    private var isScrolling = false // Флаг для отслеживания прокрутки
    private var startX = 0f // Начальная координата X касания
    private var startY = 0f // Начальная координата Y касания


    @SuppressLint("CutPasteId")
    override fun onStart() {
        super.onStart()
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->

            val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            Log.d("CustomNoteActivity", "hK=${keyboardHeight - navigationBarHeight}")
            if (keyboardHeight-navigationBarHeight > 100)
                binding.ETContent.setPadding(0, 0, 0, keyboardHeight - navigationBarHeight)
            else
                binding.ETContent.setPadding(0, 0, 0, 0)

            insets
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("NoteActivity", "Start")

        //Создание нужных переменных
        var isUpdate = false
        intent_out = Intent()
        var idNote: Int? = null


        //Создание кнопки назад в toolbar
        setSupportActionBar(binding.toolbarNote)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Устанавливаем время
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        //получение обьекта для изменения
        var inp_note = intent.getSerializableExtra("note") as Note?
        searchQuery = intent.getStringExtra("query")
        if (inp_note is Note)
        {
            Log.d("NoteActivity", "Заполнение полей")
            idNote=inp_note.id
            //Заполнение полей
            binding.apply {
                ETTitle.setText(highlightMatches(inp_note.title,searchQuery))
                ETContent.setText(highlightMatches(inp_note.content,searchQuery))
                TVDate.setText(inp_note.date)
                isUpdate = true

                // Прокрутка к первому совпадению в content
                val firstMatchPos = findFirstMatchPosition(inp_note.content, searchQuery)
                Log.d("NoteActivity2", "firstMatchPos $firstMatchPos")
                if (firstMatchPos != -1) {
                    scrollToPosition(ETContent, firstMatchPos)
                }

                //очистка подсветки если пользователь нажал на текст
                ETTitle.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && searchQuery != null) {
                        ETTitle.text = Editable.Factory.getInstance().newEditable(inp_note.title)
                        ETContent.text = Editable.Factory.getInstance().newEditable(inp_note.content)
                        searchQuery = null
                    }
                }
                ETContent.setOnFocusChangeListener{_, hasFocus ->
                    if (hasFocus && searchQuery != null) {
                        ETContent.text = Editable.Factory.getInstance().newEditable(inp_note.content)
                        ETTitle.text = Editable.Factory.getInstance().newEditable(inp_note.title)
                        searchQuery = null
                    }
                }
            }
        }
        else
        {
            binding.TVDate.setText(formattedDateTime)
        }

        //Обработчик нажатия кнопки check_mark (Ok)
        binding.toolbarNote.setOnMenuItemClickListener {item ->
            when(item.itemId)
            {
                R.id.check_mark ->
                {
                    Log.d("NoteActivity","Click btn 'Ok'")

                    // получение значения полей
                    val title = binding.ETTitle.text.toString().trim()
                    val content = binding.ETContent.text.toString().trim()
                    val date = binding.TVDate.text.toString().trim()

                    // проверка на заполненость полей
                    if(title == "" || content == "")
                        Toast.makeText(this,"Не все поля заполнены", Toast.LENGTH_SHORT).show()
                    else {
                        val db = DbHelper(this,null)
                        if (isUpdate)
                        {
                            val note = Note(
                                id = idNote,
                                title = title,
                                content = content,
                                date = formattedDateTime
                            )
                            db.updateNotes(note)
                            intent_out.apply {
                                putExtra("note",note)
                                putExtra("position",intent.getIntExtra("position",-1))
                            }
                            resultCode = 200
                            Log.d("NoteActivity","Изменина заметка $note")
                        }
                        else
                        {
                            val note = Note(null,title,content,date)
                            //Режим создания
                            idNote = db.addNote(note)
                            note.id = idNote

                            intent_out.apply {
                                putExtra("note",note)
                            }
                            resultCode = RESULT_OK
                            isUpdate = true
                            Log.d("NoteActivity","Создана заметка $note")
                        }
                    }

                    disableEditing(binding.ETContent)
                    disableEditing(binding.ETTitle)
                    true
                }
                else -> true
            }
        }

//        Обработчик EditText Title
        binding.ETTitle.setOnTouchListener {v,event ->
            when(event.action)
            {
                MotionEvent.ACTION_DOWN ->
                {
                    // Запоминаем начальные координаты касания
                    startX = event.x
                    startY = event.y
                    isScrolling = false // Сбрасываем флаг прокрутки
                }

                MotionEvent.ACTION_UP ->
                {

                    // Переключаем в режим редактирования
                    enableEditing(binding.ETTitle)
                    // Устанавливаем курсор в место касания
                    val offset = binding.ETTitle.getOffsetForPosition(event.x, event.y)
                    binding.ETTitle.setSelection(offset)
                    // Показываем клавиатуру
                    showKeyboard(binding.ETTitle)
                }
            }
            false
        }

        //Обработчик EditText Content
        binding.ETContent.setOnTouchListener { v, event ->
            when(event.action)
            {
                MotionEvent.ACTION_DOWN ->
                {
                    // Запоминаем начальные координаты касания
                    startX = event.x
                    startY = event.y
                    isScrolling = false // Сбрасываем флаг прокрутки
                }
                MotionEvent.ACTION_MOVE ->
                {
                    if(abs(event.x - startX) > 10 || abs(event.y - startY)>10)
                    {
                        isScrolling = true
                    }
                }
                MotionEvent.ACTION_UP ->
                {
                    if(!isScrolling)
                    {
                        // Переключаем в режим редактирования
                        enableEditing(binding.ETContent)
                        // Устанавливаем курсор в место касания
                        val offset = binding.ETContent.getOffsetForPosition(event.x, event.y)


                        binding.ETContent.setSelection(offset)
                        // Показываем клавиатуру
                        showKeyboard(binding.ETContent)
                    }
                }
            }
            false
        }

    }



//    //Создание меню
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        Log.d("NoteActivity","onCreateOptionsMenu")
//        if (currentFocus is EditText )
//            menuInflater.inflate(R.menu.toolbar_notes, menu)
//        this.menu = menu!!
//        return true
//    }
//
//    private fun createMenu() {
//        Log.d("NoteActivity","createMenu")
//        menu.clear()
//        invalidateOptionsMenu()
//    }
//
//
//    //Обработка toolbar кнопок
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                // Обработка нажатия на кнопку "Назад"
//
//                setResult(resultCode,intent_out)
//                finish()
//                true
//            }
//            else -> true
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
//
//    //Включение режима редактирования
//    private fun enableEditing(editText: EditText) {
//        Log.d("NoteActivity","enableEditing text: ${editText.text}")
//
//
//        editText.isFocusableInTouchMode = true
//        editText.isFocusable = true
//        editText.isCursorVisible = true
//        editText.requestFocus()
//
//
//        // Открыть клавиатуру
//        showKeyboard(editText)
//        createMenu()
//    }
//
//    //Выключение режима редактирования
//    private fun disableEditing(editText: EditText) {
//        Log.d("NoteActivity","disableEditing text: ${editText.text}")
//        editText.isFocusable = false
//        editText.isFocusableInTouchMode = false
//        editText.isCursorVisible = false
//        editText.clearFocus()
//
//        // Скрыть клавиатуру
//        hideKeyboard(editText)
//
//        menu.clear()
//    }
//
//    //метод получения индекса первого найденого тескта поиска
//    private fun findFirstMatchPosition(text: String, query: String?): Int {
//        Log.d("NoteActivity","findFirstMatchPosition")
//        if (query.isNullOrEmpty()) return -1
//        return text.lowercase(Locale.getDefault())
//            .indexOf(query.lowercase(Locale.getDefault()))
//    }
//
//    //Метод прокрутки в editText до нужного текста
//    private fun scrollToPosition(editText: EditText, position: Int) {
//        Log.d("NoteActivity","scrollToPosition")
//        editText.post {
//            editText.viewTreeObserver.addOnGlobalLayoutListener(
//                object : ViewTreeObserver.OnGlobalLayoutListener {
//                    override fun onGlobalLayout() {
//                        editText.layout?.let { layout ->
//                            if (position < 0 || position >= layout.text.length) return
//                            val line = layout.getLineForOffset(position)
//                            // Получаем верхнюю границу строки
//                            val targetY = layout.getLineTop(line)
//
//                            // Текущая высота видимой области
//                            val visibleHeight = editText.height
//
//                            // Если текст короче видимой области — не прокручиваем
//                            if (layout.height <= visibleHeight) return
//
//                            // Прокручиваем так, чтобы строка была в центре видимой области
//                            val scrollY = targetY - visibleHeight / 2
//
//                            // Ограничиваем прокрутку в пределах текста
//                            val maxScroll = layout.height - visibleHeight
//                            val finalScroll = scrollY.coerceIn(0, maxScroll)
//
//                            editText.scrollTo(0, finalScroll)
//                        }
//                        editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    }
//                }
//            )
//        }
//    }
//
//    //Открытие клавитуры
//    private fun showKeyboard(view: View){
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT)
//    }
//
//    //Закрытие клавиатуры
//    private fun hideKeyboard(view: View){
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken,0)
//    }

    override fun onBackPressed() {

        val currentFocus = currentFocus
        if (currentFocus != null)
        {
            disableEditing(binding.ETTitle)
            disableEditing(binding.ETContent)
        }
        else
        {
            setResult(resultCode,intent_out)
            finish()
            super.onBackPressed()
        }

    }




}