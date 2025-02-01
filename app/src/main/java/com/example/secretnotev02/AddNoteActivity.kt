package com.example.secretnotev02

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.databinding.ActivityAddNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddNoteActivity : BaseActivity() {

    lateinit var binding: ActivityAddNoteBinding

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

        //получение обьекта для изменения
        var inp_note = intent.getSerializableExtra("note") as Note?
        if (inp_note is Note)
        {
            //Заполнение полей
            binding.apply {
                ETTitle.setText(inp_note.title)
                ETContent.setText(inp_note.content)
                TVDate.setText(inp_note.date)
                isUpdate = true

            }
        }
        else
        {

            //Устанавливаем время
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            val formattedDateTime = currentDateTime.format(formatter)
            binding.TVDate.setText(formattedDateTime)

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
                    val note = Note(inp_note!!.id,title,content,date)
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
}