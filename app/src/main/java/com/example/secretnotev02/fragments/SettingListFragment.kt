package com.example.secretnotev02.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.example.secretnotev02.ActivityCounter
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SettingListFragment", "keys = ${AppData.AES?.Keys?.map { it.map { it.toString() } }}}")
        Log.d("SettingListFragment", "activityCount ${ActivityCounter.activityCount}")
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
}