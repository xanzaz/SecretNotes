package com.example.secretnotev02

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.databinding.ActivityNoteBinding
import com.example.secretnotev02.scripts.highlightMatches
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class NoteActivity : BaseActivity() {

    private lateinit var binding: ActivityNoteBinding
    private lateinit var menu: Menu
    private var searchQuery: String? = null

    private var resultCode: Int = RESULT_CANCELED
    private lateinit var intent_out: Intent

    private lateinit var rootView: View
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private lateinit var gestureDetector: GestureDetector

    private var isSelectingText = false // Флаг для отслеживания выделения



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

        // Инициализация GestureDetector
//
//        setupEditTextGestures()
//        disableEditing(binding.ETTitle)
//        disableEditing(binding.ETContent)

        //Создание нужных переменных
        var isUpdate: Boolean = false
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
            idNote=inp_note.id!!
            //Заполнение полей
            binding.apply {
                ETTitle.setText(highlightMatches(inp_note.title,searchQuery))
                ETContent.setText(highlightMatches(inp_note.content,searchQuery))
                TVDate.setText(inp_note.date)
                isUpdate = true

                // Прокрутка к первому совпадению в content
                val firstMatchPos = findFirstMatchPosition(inp_note.content, searchQuery)
                Log.d("AddNoteActivity", "firstMatchPos $firstMatchPos")
                if (firstMatchPos != -1) {
                    scrollToPosition(ETContent, firstMatchPos)
                }

                ETTitle.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) ETTitle.text = Editable.Factory.getInstance().newEditable(inp_note.title)
                }
                ETContent.setOnFocusChangeListener{_, hasFocus ->
                    if (hasFocus) ETContent.text = Editable.Factory.getInstance().newEditable(inp_note.content)
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

    }



    //Создание меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (currentFocus is EditText )
            menuInflater.inflate(R.menu.toolbar_notes, menu)
        this.menu = menu!!
        return true
    }

    private fun createMenu() {
        menu.clear()
        invalidateOptionsMenu()
    }


    //Обработка toolbar кнопок
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Обработка нажатия на кнопку "Назад"

                setResult(resultCode,intent_out)
                finish()
                true
            }
            else -> true
        }

        return super.onOptionsItemSelected(item)
    }

    //Включение режима редактирования
    private fun enableEditing(editText: EditText) {

        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.requestFocus()

        // Открыть клавиатуру
        showKeyboard(editText)

        createMenu()

    }

    //Выключение режима редактирования
    private fun disableEditing(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.clearFocus()

        // Скрыть клавиатуру
        hideKeyboard(editText)


        menu.clear()
    }

    //метод получения индекса первого найденого тескта поиска
    private fun findFirstMatchPosition(text: String, query: String?): Int {
        if (query.isNullOrEmpty()) return -1
        return text.lowercase(Locale.getDefault())
            .indexOf(query.lowercase(Locale.getDefault()))
    }

    //Метод прокрутки в editText до нужного текста
    private fun scrollToPosition(editText: EditText, position: Int) {
        editText.post {
            editText.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        editText.layout?.let { layout ->
                            if (position < 0 || position >= layout.text.length) return
                            val line = layout.getLineForOffset(position)
                            // Получаем верхнюю границу строки
                            val targetY = layout.getLineTop(line)

                            // Текущая высота видимой области
                            val visibleHeight = editText.height

                            // Если текст короче видимой области — не прокручиваем
                            if (layout.height <= visibleHeight) return

                            // Прокручиваем так, чтобы строка была в центре видимой области
                            val scrollY = targetY - visibleHeight / 2

                            // Ограничиваем прокрутку в пределах текста
                            val maxScroll = layout.height - visibleHeight
                            val finalScroll = scrollY.coerceIn(0, maxScroll)

                            editText.scrollTo(0, finalScroll)
                        }
                        editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
        }
    }

    //Открытие клавитуры
    private fun showKeyboard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT)
    }

    //Закрытие клавиатуры
    private fun hideKeyboard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupEditTextGestures() {
        binding.apply {
            gestureDetector = GestureDetector(
                this@NoteActivity,
                object : GestureDetector.SimpleOnGestureListener()
                {
                    override fun onDown(e: MotionEvent): Boolean {

                        return true
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        enableEditing(ETContent)
                        val offset = ETContent.getOffsetForPosition(e.x,e.y)
                        ETContent.setSelection(offset)
                        Log.d("gestureDetector", "${e.x}")
                        return true
                    }


                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float
                    ): Boolean {
                        if(isSelectingText)
                        {
                            Log.d("onScroll", "isSelectingText")
                            val offset = ETContent.getOffsetForPosition(e2.x, e2.y)
                            ETContent.setSelection(ETContent.selectionStart, offset)

                            return true
                        }
                        else
                        {
                            // Текущая позиция прокрутки
                            val currentScrollY = ETContent.scrollY

                            // Новая позиция прокрутки
                            val newScrollY = currentScrollY + distanceY.toInt()

                            // Вычисляем максимальную прокрутку
                            val maxScrollY = ETContent.layout.height - ETContent.height

                            // Ограничиваем новую позицию прокрутки
                            val clampedScrollY = newScrollY.coerceIn(0, maxScrollY)

                            // Прокручиваем до новой позиции
                            ETContent.scrollTo(ETContent.scrollX, clampedScrollY)

                            // Логируем для отладки
                            Log.d("onScroll", "Current: $currentScrollY, New: $newScrollY, Clamped: $clampedScrollY, Max: $maxScrollY")


                            return true
                        }
                    }

                    override fun onLongPress(e: MotionEvent) {
                        val offset = ETContent.getOffsetForPosition(e.x,e.y)
                        ETContent.setSelection(offset)
                        isSelectingText = true // Флаг для отслеживания выделения
                        Log.d("onLongPress", "start long press")
                    }

                }
            )
        }


        val touchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    isSelectingText = false // Завершаем выделение

                }
            }
            // Передаем событие в GestureDetector
            gestureDetector.onTouchEvent(event)

            // Возвращаем false, чтобы не перехватывать события
//            false
        }

//        binding.ETTitle.setOnTouchListener(touchListener)
        binding.ETContent.setOnTouchListener(touchListener)
    }




}