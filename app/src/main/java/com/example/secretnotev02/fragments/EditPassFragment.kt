package com.example.secretnotev02.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secretnotes.scripts.AES.AES
import com.example.secretnotes.scripts.Pref
import com.example.secretnotes.scripts.sha256
import com.example.secretnotes.scripts.sha512
import com.example.secretnotev02.DB.DbHelper
import com.example.secretnotev02.DB.SecretNote
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.SettingLayoutActivity
import com.example.secretnotev02.databinding.FragmentEditPassBinding
import com.example.secretnotev02.scripts.AppData

class EditPassFragment : Fragment() {
    lateinit var binding: FragmentEditPassBinding
    lateinit var settingActivity: SettingLayoutActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPassBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Создание кнопки назад в toolBar
        (requireActivity() as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbarEditPass))
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {

            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHasOptionsMenu(true)
        }

        //обьект SettingLayoutActivity для изменения Fragment
        settingActivity = requireActivity() as SettingLayoutActivity

        // Обработка изменения пароля
        binding.apply {
            BtnEditPass.setOnClickListener {
                //переменные полей
                val pref = Pref(view.context)
                val hashPass = pref.getValue("hashPass","")
                val oldPass = ETOldPassEditPass.text.toString().trim()
                val newPass = ETNewPassEditPass.text.toString().trim()
                val repeateNewPass = ETRepeateNewPassEditPass.text.toString().trim()

                //Проверка на наличие пароля
                if(hashPass != "")
                {
                    val hashOldPass = sha512(oldPass)
                    //Проверка совпадения старого пароля с введенным паролем
                    if(hashPass == hashOldPass)
                    {
                        //Проверка новых паролей
                        if(newPass == repeateNewPass)
                        {
                            // перекодирование секретных заметок
                            val hashNewPass = sha512(newPass)
                            pref.saveValue("hashPass",hashNewPass)

                            val hashNewPassForAES = sha256(newPass)
                            val db = DbHelper(view.context,null)

                            // Создаем объек шифровок если он не был создан
                            if (AppData.AES == null)
                                AppData.AES = AES(sha256(oldPass))

                            // получение всех заметок и перекодируем с новым паролем
                            val secretNotes = db.allSecretNotes().map {
                                SecretNote(
                                    id = it.id,
                                    title = AppData.AES!!.reEncodingText(it.title,hashNewPassForAES),
                                    content  = AppData.AES!!.reEncodingText(it.title,hashNewPassForAES),
                                    date = AppData.AES!!.reEncodingText(it.date,hashNewPassForAES)
                                )
                            }

                            // Псохраняем новый пароль
                            AppData.AES!!.updateKeys(hashNewPassForAES)

                            // Перезаписываем заметки в БД
                            for (note in secretNotes)
                            {
                                db.updateSecretNotes(note)
                            }


                            Toast.makeText(view.context, "Пароль изменен", Toast.LENGTH_SHORT).show()
                            settingActivity.loadFragment(SettingListFragment())
                        }
                        else
                            Toast.makeText(view.context, "Новые пароль не совпадают", Toast.LENGTH_SHORT).show()
                    }
                    else Toast.makeText(view.context, "Старый пароль не правильный", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }


    companion object {
        @JvmStatic
        fun newInstance() = EditPassFragment()
    }

    //Обработчик кнопки назад
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            settingActivity.loadFragment(SettingListFragment())
        }
        return super.onOptionsItemSelected(item)
    }



}