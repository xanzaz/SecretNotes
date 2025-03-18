package com.example.secretnotev02.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import com.example.secretnotes.scripts.Pref
import com.example.secretnotes.scripts.sha512
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.SettingLayoutActivity
import com.example.secretnotev02.databinding.FragmentSettingListBinding
import com.example.secretnotev02.scripts.AppData
import com.example.secretnotev02.scripts.exportSecretNotes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SettingListFragment : Fragment() {

    lateinit var binding: FragmentSettingListBinding
    private lateinit var settingActivity: SettingLayoutActivity
    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 101
    private lateinit var temp_fun:  ()->Unit

    private lateinit var  pref : Pref
    private var position_item_spener: Int = 0


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //обработка возврата из LoginFragment
        parentFragmentManager.setFragmentResultListener("request",this)
        {key,bundle ->
            if (key == "request")
            {
                if(bundle.getString("kode")=="200")
                //Если прошла авторизация
                {
//                    checkAndRequestWritePermission()
                    temp_fun()
                }

            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingListBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Создание переменных
        pref = Pref(requireContext())
        position_item_spener = pref.getValue("AES_position",0)


        //Создание кнопки назад в toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar_setting))
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {

            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHasOptionsMenu(true)
        }

        settingActivity = requireActivity() as SettingLayoutActivity


        //нажатие на изменить пароль
        binding.CVeditPass.setOnClickListener{
            settingActivity.loadFragment(EditPassFragment())
        }
        //нажатие на экспортировать все секретные заметки
        binding.CVExportSecretNote.setOnClickListener {
            if(AppData.AES == null)
            {
                val fragment = LoginFragment().apply {
                    arguments = bundleOf(
                        "calling_function" to "SettingListFragment"
                    )
                }
                temp_fun = { checkAndRequestWritePermission() }
                parentFragmentManager.commit {
                    replace(R.id.FrameSettingLayout,fragment)
                    addToBackStack(null)
                }
            }
            else
            {
                // экспортируем все секретные заметки
                checkAndRequestWritePermission()
            }
        }
        //нажатие на импортировать заметки
        binding.CVImportSecretNote.setOnClickListener {
            if(AppData.AES == null)
            {
                val fragment = LoginFragment().apply {
                    arguments = bundleOf(
                        "calling_function" to "SettingListFragment"
                    )
                }
                temp_fun = { openImportFragment() }
                parentFragmentManager.commit {
                    replace(R.id.FrameSettingLayout,fragment)
                    addToBackStack(null)
                }
            }
            else{
                openImportFragment()
            }
//            settingActivity.loadFragment(ImportSecretNotesFragment())
//            parentFragmentManager.commit {
//                replace(R.id.FrameSettingLayout,ImportSecretNotesFragment())
//                addToBackStack(null)
//            }
        }

        // Код выпадающего списка
        var isUserInteractionSpinner = false
        val adapter_spiner = ArrayAdapter.createFromResource(requireContext(),R.array.AES_version, android.R.layout.simple_spinner_item)
        adapter_spiner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAes.adapter = adapter_spiner

        Log.d("SettingListFragment","position: $position_item_spener")
        binding.spinnerAes.setSelection(position_item_spener)

        binding.spinnerAes.setOnTouchListener{ v, event ->
            if(event.action == MotionEvent.ACTION_UP)
            {
                isUserInteractionSpinner = true
            }
            false
        }

        binding.spinnerAes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d("SettingListFragment","onItemSelectedListener position: ${position}")
                if(isUserInteractionSpinner)
                {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    // Выполните действия при выборе элемента
                    Toast.makeText(requireContext(), "Выбрано: $selectedItem", Toast.LENGTH_SHORT).show()
                    showConfirmationAesDialog(selectedItem)
                    isUserInteractionSpinner = false
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }



    }

//обработчик нажатия кнопки назад
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startActivity(Intent(this.context,MainActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingListFragment()
    }
    //экспорт всех секретных заметок
    @RequiresApi(Build.VERSION_CODES.O)
    private fun exportAllSecretNotes()
    {
        val db = DbHelper(settingActivity,null)
        val note_list = db.allSecretNotes().map { it.toNote() }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        exportSecretNotes(settingActivity,"AllNotes $formattedDateTime",note_list)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            exportAllSecretNotes()

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportAllSecretNotes()
                } else {
                    Toast.makeText(requireContext(), "Разрешение отклонено", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openImportFragment()
    {
        settingActivity.loadFragment(ImportSecretNotesFragment())
    }

    private fun showConfirmationAesDialog(item_name: String)
    {
        Log.d("SettingListFragment","showConfirmationAesDialog:\n старые коэф:\n position = ${pref.getValue("AES_position",-1)}\n Nb=${pref.getValue("AES_Nb",-1)}\n Nk=${pref.getValue("AES_Nk",-1)}\n Nr=${pref.getValue("AES_Nr",-1)}")
        // Инфлейтим кастомный макет
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_style_dialog_confirmation_aes, null)



        val messageDialog = dialogView.findViewById<TextView>(R.id.messageDialog)
        val btn_yes = dialogView.findViewById<Button>(R.id.btn_yes)
        val btn_no = dialogView.findViewById<Button>(R.id.btn_no)

        messageDialog.setText("Вы уверены изменить версию шифрования на $item_name?")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btn_yes.setOnClickListener {
            val hashpass = pref.getValue("hashPass","")
            val pass = dialog.findViewById<EditText>(R.id.ET_PassDialog)?.text.toString().trim()
            if (hashpass != "")
            {
                if(pass != "")
                {
                    if(hashpass == sha512(pass))
                    {
                        Toast.makeText(requireContext(), "yes", Toast.LENGTH_SHORT).show()
                        val newPosition = binding.spinnerAes.selectedItemPosition
                        var Nb: Int? = null
                        var Nk: Int? = null
                        var Nr: Int? = null
                        when(newPosition)
                        {
                            0-> { Nb = 4; Nk = 4; Nr = 10 }
                            1-> { Nb = 4; Nk = 6; Nr = 12 }
                            2-> { Nb = 4; Nk = 8; Nr = 14 }
                        }
                        if (Nb != null && Nk != null && Nr != null  )
                        {
                            pref.saveValue("AES_position",newPosition)
                            pref.saveValue("AES_Nb",Nb )
                            pref.saveValue("AES_Nk",Nk)
                            pref.saveValue("AES_Nr",Nr)
                        }


                        position_item_spener = newPosition
                        dialog.dismiss()
                    }
                    else Toast.makeText(requireContext(), "Пароль не правильный", Toast.LENGTH_SHORT).show()
                }
                else Toast.makeText(requireContext(), "Введите пароль", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(requireContext(), "У вас не создан пароль", Toast.LENGTH_SHORT).show()


        }
        btn_no.setOnClickListener {
            Toast.makeText(requireContext(), "no", Toast.LENGTH_SHORT).show()
            binding.spinnerAes.setSelection(position_item_spener)
            dialog.dismiss()
        }

        dialog.show()


    }
}

