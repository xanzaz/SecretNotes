package com.example.secretnotev02

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.DB.SecretNote
import com.example.secretnotev02.customActivity.CustomNoteActivity
import com.example.secretnotev02.databinding.ActivityNoteBinding
import com.example.secretnotev02.scripts.AppData
import com.example.secretnotev02.scripts.highlightMatches
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class SecretNoteActivity : CustomNoteActivity() {

    lateinit var binding: ActivityNoteBinding
    private var searchQuery: String? = null

    private var isScrolling = false // Флаг для отслеживания прокрутки
    private var startX = 0f // Начальная координата X касания
    private var startY = 0f // Начальная координата Y касания

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

        Log.d("SecretNoteActivity", "Start")
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

        //Получение обьекта для изменения
        var inp_note = intent.getSerializableExtra("note") as Note?
        searchQuery = intent.getStringExtra("query")
        Log.d("SecretNoteActivity", "searchQuery = $searchQuery")
        if (inp_note is Note)
        {
            Log.d("SecretNoteActivity", "Заполнение полей")
            idNote = inp_note.id
            //Заполнение полей данными
            binding.apply {
                ETTitle.setText(highlightMatches(inp_note.title.trim(), searchQuery))
                ETContent.setText(highlightMatches(inp_note.content.trim(), searchQuery))
                TVDate.setText(inp_note.date.trim())
                isUpdate = true

                // Прокрутка к первому совпадению в content
                val firstMatchPos = findFirstMatchPosition(inp_note.content, searchQuery)
                Log.d("SecretNoteActivity", "firstMatchPos $firstMatchPos")
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


        binding.toolbarNote.setOnMenuItemClickListener {item ->
            when(item.itemId)
            {
                R.id.check_mark ->
                {
                    Log.d("SecretNoteActivity", "Click btn 'Ok'")
                    // получение значения полей
                    val title = binding.ETTitle.text.toString().trim()
                    val content = binding.ETContent.text.toString().trim()
                    val date = binding.TVDate.text.toString().trim()

                    // проверка на заполненость полей
                    if(title == "" || content == "")
                        Toast.makeText(this,"Не все поля заполнены", Toast.LENGTH_SHORT).show()
                    else {

                        val db = DbHelper(this,null)
                        //Обновление обьекта
                        if (isUpdate)
                        {
                            val secretNote = SecretNote(
                                id = idNote,
                                title = AppData.AES!!.encodingText(title),
                                content = AppData.AES!!.encodingText(content),
                                date = AppData.AES!!.encodingText(formattedDateTime))

                            val note = Note(idNote,title,content,formattedDateTime)
                            db.updateSecretNotes(secretNote)
                            //Передача заметки обратно на фрагмент для изменения обьекта в списке
                            intent_out.apply {
                                putExtra("note",note)
                                putExtra("position",intent.getIntExtra("position",-1))
                            }
                            resultCode = 200
                            Log.d("SecretNoteActivity","Изменина заметка $note")
                        }
                        //Создание обьекта
                        else
                        {

                            val secretNote = SecretNote(
                                null,
                                AppData.AES!!.encodingText(title),
                                AppData.AES!!.encodingText(content),
                                AppData.AES!!.encodingText(date))

                            idNote = db.addSecretNote(secretNote)
                            val note = Note(idNote,title,content,date)

                            //Передача заметки обратно на фрагмент для создания обьекта в списке
                            intent_out.apply {
                                putExtra("note",note)
                            }
                            resultCode = RESULT_OK
                            isUpdate = true
                            Log.d("SecretNoteActivity","Создана заметка $note")
                        }
                    }
                    disableEditing(binding.ETTitle)
                    disableEditing(binding.ETContent)
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
                    if(abs(event.x - startX) > 10 || abs(event.y - startY) >10)
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