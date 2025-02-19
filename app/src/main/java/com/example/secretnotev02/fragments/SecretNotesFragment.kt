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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secretnotev02.SecretNoteActivity
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.Note
import com.example.secretnotev02.DB.NoteTable
import com.example.secretnotev02.MainActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.widget.SearchView
import com.example.secretnotev02.R
import com.example.secretnotev02.adapters.NoteAdapter

import com.example.secretnotev02.databinding.FragmentSecretNotesBinding
import com.example.secretnotev02.scripts.exportSecretNotes


class SecretNotesFragment : Fragment(), NoteAdapter.OnItemInteractionListener{

    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101

    lateinit var binding: FragmentSecretNotesBinding
    private lateinit var adapter : NoteAdapter
    private lateinit var addLauncher: ActivityResultLauncher<Intent>
    lateinit var mainActivity: MainActivity

    private var selectedItems = mutableSetOf<NoteTable>()
    private var isClearSelectedItem: Boolean = false

    private var query: String? = null

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
        val notesTableList = db.allSecretNotes().map { it.toNote().toNoteTable() }.toMutableList()

        adapter = NoteAdapter(notesTableList.toMutableList())
        adapter.setListener(this)

        //обработка полученных данных из активити
        addLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            // создание заметки
            if(it.resultCode == RESULT_OK) {
//                adapter.addNote((it.data?.getSerializableExtra("note") as Note).toNoteTable())
                notesTableList.add((it.data?.getSerializableExtra("note") as Note).toNoteTable())
                adapter.updateList(notesTableList,query)
            }
            //обновление заметки
            else if (it.resultCode == 200) {
                val index_note: Int
                val position_out = it.data?.getIntExtra("position",-2)
                val note_out = it.data?.getSerializableExtra("note") as Note
                if (position_out!! >=  0)
                {
                    index_note = notesTableList.binarySearch {it.id - note_out.id!!}
                    notesTableList[index_note] = note_out.toNoteTable()
                    adapter.updateList(notesTableList,query)
//                    adapter.updateNote(position_out,note_out.toNoteTable())
                }
                else if (position_out == -1)
                {
                    notesTableList.add((it.data?.getSerializableExtra("note") as Note).toNoteTable())
                    adapter.updateList(notesTableList,query)
//                    adapter.addNote(note_out.toNoteTable())
                }
            }
        }

        binding.apply {
            //Добавление созданного адаптера в список
            RVSecretNotes.layoutManager = LinearLayoutManager(view.context)
            RVSecretNotes.adapter = adapter

            //Запуск активити для создания
            ABAddSecretNote.setOnClickListener {
                selectedItems.clear()
                adapter.clearAllActived()
                changeMenu()
                addLauncher.launch(Intent(view.context,SecretNoteActivity::class.java))
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
            val intent = Intent(this.context,SecretNoteActivity()::class.java)
            intent.putExtra("note",noteTable.toNote())
            intent.putExtra("position",position)
            intent.putExtra("query", query)
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
                    //Импортировать
                    R.id.import_selected_secret_notes_menu ->
                    {
                        checkAndRequestWritePermission()
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

    private fun exportNote()
    {
        exportSecretNotes(mainActivity,"Notes",selectedItems.map { it.toNote() }.toList())
    }


    private fun checkAndRequestWritePermission()
    {
        if(ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        )
        {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        }
        else
        {
            exportNote()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportNote()
                } else {
                    Toast.makeText(requireContext(), "Разрешение отклонено", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




}