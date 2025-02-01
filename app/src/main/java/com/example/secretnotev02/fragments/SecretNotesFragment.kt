package com.example.secretnotev02.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secretnotev02.AddSecretNoteActivity
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.DB.NoteTable
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.adapters.NoteAdapter

import com.example.secretnotev02.databinding.FragmentSecretNotesBinding


class SecretNotesFragment : Fragment(), NoteAdapter.OnItemInteractionListener{
    lateinit var binding: FragmentSecretNotesBinding
    private lateinit var adapter : NoteAdapter
    private lateinit var addLauncher: ActivityResultLauncher<Intent>
    lateinit var mainActivity: MainActivity

    private var selectedItems = mutableSetOf<NoteTable>()
    private var isClearSelectedItem: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecretNotesBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity

        val db = DbHelper(view.context,null)
        val notesTableList = db.allSecretNotes().map { it.toNote().toNoteTable() }

        adapter = NoteAdapter(notesTableList.toMutableList())
        adapter.setListener(this)

        //обработка полученных данных из активити
        addLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            // создание заметки
            if(it.resultCode == RESULT_OK) {
                adapter.addNote((it.data?.getSerializableExtra("note") as Note).toNoteTable())
            }
            //обновление заметки
            else if (it.resultCode == 200) {
                val position_out = it.data?.getIntExtra("position",-1)?: -1
                val note_out = it.data?.getSerializableExtra("note") as Note
                if (position_out!! >=  0)
                {
                    adapter.updateNote(position_out,note_out.toNoteTable())
                }
            }
        }

        binding.apply {
            //Добавление созданного адаптера в список
            RVSecretNotes.layoutManager = LinearLayoutManager(view.context)
            RVSecretNotes.adapter = adapter

            //Запуск активити для создания
            ABAddSecretNote.setOnClickListener {
                addLauncher.launch(Intent(view.context,AddSecretNoteActivity::class.java))
            }

        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = SecretNotesFragment()
    }

//обработчик нажатия на элемент списка
    override fun onItemClick(position: Int, noteTable: NoteTable, view: View) {
        if (selectedItems.isNotEmpty())
        {
            if (selectedItems.contains(noteTable))
            {
                selectedItems.remove(noteTable)
                noteTable.isActive = false

                if (isClearSelectedItem)
                {
                    isClearSelectedItem = false
                    mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_notes_menu).setTitle("Выбрать все").setIcon(R.drawable.all_notes)
                }
                if (selectedItems.isEmpty())
                {
                    changeMenu()
                }
            }
            else
            {
                noteTable.isActive = true
                selectedItems.add(noteTable)



                if (selectedItems.size == adapter.itemCount)
                {
                    isClearSelectedItem = true
                    mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_secret_notes_menu).setTitle("Очистить").setIcon(R.drawable.clear_notes)
                }
            }

        }
        else
        {
            val intent = Intent(this.context,AddSecretNoteActivity()::class.java)
            intent.putExtra("note",noteTable.toNote())
            intent.putExtra("position",position)
            addLauncher.launch(intent)
        }


    }

//обработчик долгого нажатия на элемент списка
    override fun onItemLongClick(position: Int, noteTable: NoteTable, view: View) {
        noteTable.isActive = true
        selectedItems.add(noteTable)
        changeMenu()
    }

//Метод изменения меню и обработка нажатия на кнопок
    private fun changeMenu()
    {
        //Меню для выбранных элементов
        if(selectedItems.isNotEmpty())
        {
            mainActivity.onChangeMenu(R.menu.bottom_nav_selected_secret_notes)
            //Назначение обработчика кнопок
            mainActivity.bottomNavView.setOnItemSelectedListener { item->
                when(item.itemId)
                {
                    //Выбрать все/Очистить
                    R.id.all_clear_selected_secret_notes_menu -> {
                        if(isClearSelectedItem)
                        {
                            for (noteTable in adapter.getNotesTable())
                                noteTable.isActive = false
                            selectedItems.clear()
                            adapter.notifyDataSetChanged()
                            isClearSelectedItem = false
                            changeMenu()


                        }
                        else
                        {
                            val notesTable = adapter.getNotesTable()
                            for (noteTable in notesTable)
                            {
                                noteTable.isActive = true
                            }
                            selectedItems = notesTable.toMutableSet()
                            isClearSelectedItem = true
                            mainActivity.bottomNavView.menu.findItem(R.id.all_clear_selected_secret_notes_menu).setTitle("Очистить").setIcon(R.drawable.clear_notes)
                            adapter.notifyDataSetChanged()
                        }
                        true
                    }
                    //Удалить
                    R.id.delete_selected_secret_notes_menu -> {
                        val db = DbHelper(mainActivity,null)

                        adapter.deleteNotes(selectedItems.toList())

                        for (noteTable in selectedItems)
                        {
                            db.deleteSecretNote(noteTable.id)
                        }
                        selectedItems.clear()
                        changeMenu()

                        true
                    }

                    else -> true
                }
            }
        }
        //Меню главного экрана
        else
        {
            mainActivity.onChangeMenu(R.menu.bottom_navigation_main)
            mainActivity.bottomNavView.setOnItemSelectedListener { item-> mainActivity.listenerMain(item.itemId) }
            mainActivity.bottomNavView.selectedItemId = R.id.nav_main_secret_notes
        }
    }

}