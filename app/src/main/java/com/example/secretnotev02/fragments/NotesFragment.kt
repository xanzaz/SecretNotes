package com.example.secretnotev02.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secretnotev02.AddNoteActivity
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.adapters.NoteAdapter
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.DB.NoteTable
import com.example.secretnotev02.DB.SecretNote
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.NoteActivity
import com.example.secretnotev02.NoteActivity2
import com.example.secretnotev02.R
import com.example.secretnotev02.databinding.FragmentNotesBinding
import com.example.secretnotev02.scripts.AppData


class NotesFragment : Fragment(), NoteAdapter.OnItemInteractionListener {

    lateinit var binding: FragmentNotesBinding
    private lateinit var adapter : NoteAdapter
    private lateinit var addLauncher: ActivityResultLauncher<Intent>
    lateinit var mainActivity: MainActivity

    private var selectedItems = mutableSetOf<NoteTable>()
//    private var isSelectetItem: Boolean = false
    private var isClearSelectedItem: Boolean = false
    private var listState: Parcelable? = null

    //Текст поиска
    private var query: String? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //обработка возврата из LoginFragment
        parentFragmentManager.setFragmentResultListener("request",this) { key,bundle ->
            if (key == "request")
            {
                if(bundle.getString("kode")=="200")
                    //Если прошла авторизация то перекодируем выбранные элементы
                    for (note in selectedItems)
                        adapter.updateNote(note)
                    encryptNotes()


            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentNotesBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mainActivity = requireActivity() as MainActivity

            //получение заметок из БД и преобразовываем в NoteTable(тип данных для списка)
        val db = DbHelper(view.context,null)
        val notes_list = db.allNotes()
        val notesTableList = notes_list.map {it.toNoteTable()}



        //Создание адаптера для списка. Передача полученных заметок и обработчика
        adapter = NoteAdapter(notesTableList.toMutableList())
        adapter.setListener(this)

        //обработка получения данных из активити (NoteActivity)
        addLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            // если было создание
            if(it.resultCode == RESULT_OK)
            {
                adapter.addNote((it.data?.getSerializableExtra("note") as Note).toNoteTable())
            }
            // если было обновление
            else if(it.resultCode == 200)
            {
                val position_out = it.data?.getIntExtra("position",-2)
                val note_out = it.data?.getSerializableExtra("note") as Note
                Log.d("NotesFragment","addLauncher note_out $position_out")
                if (position_out!! >=  0)
                {
                    adapter.updateNote(position_out,note_out.toNoteTable())
                }
                else if (position_out == -1)
                    adapter.addNote(note_out.toNoteTable())
            }
        }




        binding.apply {
            //Добавление созданного адаптера в список
            RVNotes.layoutManager = LinearLayoutManager(view.context)
            RVNotes.adapter = adapter

            //Обработка надатия кнопки для создания
            ABAddNote.setOnClickListener {
//                isSelectetItem = false
                selectedItems.clear()
                adapter.clearAllActived()
//                addLauncher.launch(Intent(view.context,AddNoteActivity::class.java))
                addLauncher.launch(Intent(view.context, NoteActivity2::class.java))
                changeMenu()
            }

            //Обработчик поиска
            SvNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    query = newText
                    val filteredNoteTable = if(newText.isNullOrEmpty())
                    {
                        notesTableList
                    }
                    else
                    {
                        notesTableList.filter { noteTable ->
                            noteTable.title.contains(newText,true) || noteTable.content.contains(newText,true)
                        }
                    }
                    adapter.updateList(filteredNoteTable,newText)
                    return true
                }

            })
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }



//Обработка нажатия на элемент из списка
    override fun onItemClick(position: Int,noteTable: NoteTable, view: View) {
        // Режим выбора
        if (selectedItems.isNotEmpty()) {
            //Если элемент есть в списке то удаляем.
            //  + изменение кнопки меню если выбранны все элементы
            //  + Изменение меню если нету выбранных элементов
            if(selectedItems.contains(noteTable)) {
                selectedItems.remove(noteTable)
                noteTable.isActive = false

                if (isClearSelectedItem)
                {
                    isClearSelectedItem = false
                    mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_notes_menu).setTitle("Выбрать все").setIcon(R.drawable.all_notes)
                }
                if (selectedItems.isEmpty())
                {
//                    isSelectetItem = false
                    changeMenu()
                }
            }

            //Если нет, то добавляем
            //  + изменение кнопки меню если выбранны все элементы
            else {
                noteTable.isActive = true
                selectedItems.add(noteTable)



                if (selectedItems.size == adapter.itemCount)
                {
                    isClearSelectedItem = true
                    mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_notes_menu).apply {
                        setIcon(R.drawable.clear_notes)
                        setTitle("Очистить")
                    }


                }

            }

        }
        //Режим изменения
        else {
            //открываем окно для изменения
//            val intent = Intent(this.context,AddNoteActivity::class.java)
            val intent = Intent(this.context,NoteActivity2::class.java)
            intent.putExtra("note",noteTable.toNote())
            intent.putExtra("position",position)
            intent.putExtra("query", query)
            addLauncher.launch(intent)
        }
    }

//Обработка долгого нажатия на элемент из списка
    override fun onItemLongClick(position: Int, noteTable: NoteTable, view: View) {
        // Добавляем элемент. Изменяем меню
        noteTable.isActive = true
        selectedItems.add(noteTable)
        changeMenu()
    }

//Метод шифровки заметок
    private fun encryptNotes() {

        val db = DbHelper(mainActivity,null)

        //Выбранные элементы преобразуем в SecretNote
        val secretNotes = selectedItems.map {
            SecretNote(
                id = null,
                title = AppData.AES!!.encodingText(it.title),
                content = AppData.AES!!.encodingText(it.content),
                date = AppData.AES!!.encodingText(it.date))
        }

        //Сохраняем в таблицу secret_notes
        for (SNote in secretNotes)
            db.addSecretNote(SNote)

        //Удаляем из таблицы notes
        for (note in selectedItems)
            db.deleteNote(note.id)

        //Удаляем выбранные элементы из списка и очищаем список выбранных элементов.
        adapter.deleteNotes(selectedItems.toMutableList())
        selectedItems.clear()
        changeMenu()

    }

//Метод изменения меню и обработка нажатия на кнопок
    private fun changeMenu()
    {
        //Меню для выбранных элементов
        if(selectedItems.isNotEmpty())
        {
            mainActivity.onChangeMenu(R.menu.bottom_nav_selected_notes)
            //Назначение обработчика кнопок
            mainActivity.bottomNavView.setOnItemSelectedListener { item ->
                when(item.itemId)
                {
                    //Выбрать все/Очистить
                    R.id.all_clear_selected_notes_menu -> {

                        if(isClearSelectedItem) //Очистить
                        {
                            for (noteTable in adapter.getNotesTable())
                                noteTable.isActive = false
                            selectedItems.clear()
                            adapter.notifyDataSetChanged()
                            isClearSelectedItem = false
//                            isSelectetItem = false
                            changeMenu()


                        }
                        else //Выбрать все
                        {
                            val notesTable = adapter.getNotesTable()
                            for (noteTable in notesTable)
                            {
                                noteTable.isActive = true
                            }
                            selectedItems = notesTable.toMutableSet()
                            isClearSelectedItem = true
                            mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_notes_menu).apply {
                                setIcon(R.drawable.clear_notes)
                                setTitle("Очистить")
                            }
                            adapter.notifyDataSetChanged()
                        }
                        true
                    }
                    //Удалить
                    R.id.delete_selected_notes_menu-> {
                        val db = DbHelper( mainActivity ,null)

                        adapter.deleteNotes(selectedItems.toList())

                        for (noteTable in selectedItems)
                        {
                            db.deleteNote(noteTable.id)

                        }
//                        isSelectetItem = false
                        selectedItems.clear()
                        changeMenu()

                        true
                    }
                    //Зашифровать
                    R.id.encrypt_selected_notes_menu -> {
                        //Если не создан обьект шифрования, запускаем фрагмент для ввода пароля
                        if (AppData.AES == null) {
                            val fragment = LoginFragment().apply {
                                arguments = bundleOf(
                                    "calling_function" to "NotesFragment"
                                )
                            }
                            parentFragmentManager.commit {
                                replace(R.id.FrameLayautMain,fragment)
                                addToBackStack(null)
                            }
                        }
                        else encryptNotes()

                        true
                    }

                    else -> true
                }
            }
        }
        //Меню главного экрана
        else {
            mainActivity.onChangeMenu(R.menu.bottom_navigation_main)
            mainActivity.bottomNavView.setOnItemSelectedListener { item-> mainActivity.listenerMain(item.itemId) }
        }
    }


}