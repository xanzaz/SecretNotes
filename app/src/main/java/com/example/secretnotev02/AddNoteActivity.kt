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
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.FOCUSABLE
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.databinding.ActivityAddNoteBinding
import com.example.secretnotev02.scripts.highlightMatches
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class AddNoteActivity : BaseActivity() {

    lateinit var binding: ActivityAddNoteBinding
    private var searchQuery: String? = null

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var isUpdate = false

        //Устанавливаем время
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        //получение обьекта для изменения
        var inp_note = intent.getSerializableExtra("note") as Note?
        searchQuery = intent.getStringExtra("query")
        if (inp_note is Note)
        {
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

        //Обработка касаний для прокрутки
        binding.apply {
            // Отключаем фокус и клавиатуру
            ETContent.isFocusable = false
            ETContent.isFocusableInTouchMode = false
//            disableEditing(ETContent)

            val gestureDetector = GestureDetector(
                this@AddNoteActivity,
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
                        Log.d("onScroll", "${distanceY}")
                        ETContent.scrollBy(0,distanceY.toInt())
                        Log.d("onScroll", "${ETContent.focusable}")


                        return true
                    }
                }
            )


            ETContent.setOnTouchListener { _ , event ->
                gestureDetector.onTouchEvent(event)

            }





        }




        //обработчик нажатия кнопки "Назад". Возвращает на главную страницу
        binding.BtnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()

        }

        //обработчик нажатия кнопки "Сохранить"
        binding.BtnCreate.setOnClickListener {
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
                    val note = Note(inp_note!!.id,title,content,formattedDateTime)
                    db.updateNotes(note)
                    val intent = Intent().apply {
                        putExtra("note",note)
                        putExtra("position",intent.getIntExtra("position",-1))
                    }
                    setResult(200,intent)
                    finish()
                }
                else
                {
                    val note = Note(null,title,content,date)
                    //Режим создания
                    val id = db.addNote(note)
                    note.id = id
                    val intent = Intent().apply {
                        putExtra("note",note)
                    }
                    setResult(RESULT_OK,intent)
                    finish()
                }


            }
        }
    }

    //метод получения индекса первого найденого тескта поиска
    private fun findFirstMatchPosition(text: String, query: String?): Int {
        if (query.isNullOrEmpty()) return -1
        return text.lowercase(Locale.getDefault())
            .indexOf(query.lowercase(Locale.getDefault()))
    }

    //Метод прокрутки в editText
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

    //Включение режима редактирования
    private fun enableEditing(editText: EditText) {

        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

    }

    //Выключение режима редактирования
    private fun disableEditing(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.clearFocus()

        // Скрыть клавиатуру
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
    private fun setupKeyboardVisibilityListener(editText: EditText) {
        Log.d("setupKeyboardVisibilityListener","start")
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom
            Log.d("setupKeyboardVisibilityListener","screenHeight = $screenHeight")
            Log.d("setupKeyboardVisibilityListener","keypadHeight = $keypadHeight")
            // Если высота клавиатуры меньше 10% от высоты экрана, считаем, что она скрыта
            if (keypadHeight < screenHeight * 0.1) {
                disableEditing(editText)
            }
        }
    }


    fun isKeyboardActive(view: View): Boolean {
        val insets = ViewCompat.getRootWindowInsets(view)
        return insets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
    }
    // Скрыть клавиатуру
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Очистить фокус (cука наконец работает)
    fun clearFocus(v: View) {
        val rootView = currentFocus?.rootView // или findViewById<View>(android.R.id.content)
        rootView?.apply {
            isFocusableInTouchMode = true
            isFocusable = true
            requestFocus()
        }
        currentFocus?.clearFocus()
    }

    override fun onBackPressed() {
        val currentFocus = currentFocus
        if (currentFocus != null) {
            currentFocus.isFocusableInTouchMode = true
            currentFocus.isFocusable = true

            hideKeyboard(currentFocus)
            clearFocus(currentFocus)
        } else {
            super.onBackPressed()
        }
    }


}