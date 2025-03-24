package com.example.secretnotev02.customActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.R
import java.util.Locale

open class CustomNoteActivity: BaseActivity()
{

    private lateinit var menu: Menu
    lateinit var intent_out: Intent
    var resultCode: Int = RESULT_CANCELED

    //Создание меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d("CustomNoteActivity","onCreateOptionsMenu")
        if (currentFocus is EditText)
            menuInflater.inflate(R.menu.toolbar_notes, menu)
        this.menu = menu!!
        return true
    }
    fun createMenu() {
        Log.d("CustomNoteActivity","createMenu")
        menu.clear()
        invalidateOptionsMenu()
    }

    //Обработка toolbar кнопок
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Обработка нажатия на кнопку "Назад"
                Log.d("CustomNoteActivity","resultCode = $resultCode")
                Log.d("CustomNoteActivity","intent_out = ${intent_out.getSerializableExtra("note") as Note?}")
                setResult(resultCode, intent_out)
                finish()
                true
            }

            else -> true
        }
    }

    //Включение режима редактирования
    fun enableEditing(editText: EditText) {
        Log.d("CustomNoteActivity","enableEditing text: ${editText.text}")


        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isCursorVisible = true
        editText.requestFocus()


        // Открыть клавиатуру
        showKeyboard(editText)
        createMenu()
    }

    //Выключение режима редактирования
    fun disableEditing(editText: EditText) {
        Log.d("CustomNoteActivity","disableEditing text: ${editText.text}")
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isCursorVisible = false
        editText.clearFocus()

        // Скрыть клавиатуру
        hideKeyboard(editText)

        menu.clear()
    }
    //метод получения индекса первого найденого тескта поиска
    fun findFirstMatchPosition(text: String, query: String?): Int {
        Log.d("CustomNoteActivity","findFirstMatchPosition")
        if (query.isNullOrEmpty()) return -1
        return text.lowercase(Locale.getDefault())
            .indexOf(query.lowercase(Locale.getDefault()))
    }

    //Метод прокрутки в editText до нужного текста
    fun scrollToPosition(editText: EditText, position: Int) {
        Log.d("CustomNoteActivity","scrollToPosition")
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

                            Log.d("CustomNoteActivity","scrollToPosition scrollY $scrollY ")

                            // Ограничиваем прокрутку в пределах текста
                            val maxScroll = layout.height - visibleHeight
                            val finalScroll = scrollY.coerceIn(0, maxScroll)
                            Toast.makeText(this@CustomNoteActivity,
                                "position: $position; line: $line; targetY: $targetY; visibleHeight: $visibleHeight; lh ${layout.height}; fS: $finalScroll",Toast.LENGTH_SHORT).show()

                            editText.postDelayed({editText.scrollY = finalScroll},50L)





                        }
                        editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
        }
    }

    //Открытие клавитуры
    fun showKeyboard(view: View){
        Log.d("CustomNoteActivity","showKeyboard()")
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    //Закрытие клавиатуры
    fun hideKeyboard(view: View){
        Log.d("CustomNoteActivity","hideKeyboard()")
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }
}
