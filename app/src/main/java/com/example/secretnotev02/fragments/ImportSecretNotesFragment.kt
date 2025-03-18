package com.example.secretnotev02.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.secretnotes.scripts.AES.AES
import com.example.secretnotes.scripts.sha256
import com.example.secretnotev02.ActivityCounter
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.SecretNote
import com.example.secretnotev02.R
import com.example.secretnotev02.SettingLayoutActivity
import com.example.secretnotev02.databinding.FragmentImportSecretNotesBinding
import com.example.secretnotev02.scripts.AppData
import com.example.secretnotev02.scripts.NoteExport
import kotlinx.serialization.json.Json


class ImportSecretNotesFragment : Fragment() {

    private val REQUEST_CODE_IMPORT_FILE = 102
    private lateinit var binding: FragmentImportSecretNotesBinding
    private lateinit var settingActivity: SettingLayoutActivity
    private var fileData: ByteArray? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportSecretNotesBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingActivity = requireActivity() as SettingLayoutActivity

        binding.BtnBackImportSecretNotes.setOnClickListener {
            if(binding.ETNameFileImportSecretNotes.text.isNotEmpty())  ActivityCounter.activityStopped()
            settingActivity.loadFragment(SettingListFragment())

//            parentFragmentManager.popBackStack()
        }
        binding.BtnFileSelectionImportSecretNotes.setOnClickListener {
            openFilePicker()
        }
        binding.apply {
            BtnImport.setOnClickListener {
                if(ETNameFileImportSecretNotes.text.isEmpty())
                {
                    Toast.makeText(view.context,"Не выбран файл",Toast.LENGTH_SHORT).show()
                }
                else if (ETPassImportSecretNotes.text.isEmpty())
                {
                    Toast.makeText(view.context,"Не введен пароль",Toast.LENGTH_SHORT).show()
                }
                else
                {
                    val pass = ETPassImportSecretNotes.text.toString().trim()
                    val aes = AES(sha256(pass),requireContext())
                    val strJson = aes.decryptText(fileData as ByteArray)

                    val note_list = Json.decodeFromString<List<NoteExport>>(strJson)

                    val db = DbHelper(view.context,null)

                    if (AppData.AES != null)
                    {
                        for (note in note_list)
                        {

                        db.addSecretNote(SecretNote(
                            id = null,
                            title = AppData.AES!!.encodingText(note.title),
                            content = AppData.AES!!.encodingText(note.content),
                            date = AppData.AES!!.encodingText(note.date)
                        ))
                        }
                    }
                    Toast.makeText(view.context,"Заметки добавлены",Toast.LENGTH_SHORT).show()
                    if(binding.ETNameFileImportSecretNotes.text.isNotEmpty())  ActivityCounter.activityStopped()
                    settingActivity.loadFragment(SettingListFragment())


                }

        }






        }
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ImportSecretNotesFragment()
    }


    fun openFilePicker() {
        if (ActivityCounter.activityCount < 2)
            ActivityCounter.activityStarted()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream" // Указываем тип файла (бинарный)
        }
        startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE)
    }
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                // Получаем имя файла из колонки DISPLAY_NAME
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


//        ActivityCounter.activityStopped()

        if (requestCode == REQUEST_CODE_IMPORT_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Получаем имя файла
                val fileName = getFileNameFromUri(this.requireContext(), uri)
                // Обновляем TextView
                if (fileName != null) {
                    binding.ETNameFileImportSecretNotes.setText("$fileName")
                } else {
                    binding.ETNameFileImportSecretNotes.setText("Не удалось получить имя файла")
                }
                // Чтение данных из файла
                fileData = readFileFromUri(uri)


            }
        }
    }
    private fun readFileFromUri(uri: Uri): ByteArray? {
        return try {
            this.context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes() // Чтение файла в массив байтов
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}