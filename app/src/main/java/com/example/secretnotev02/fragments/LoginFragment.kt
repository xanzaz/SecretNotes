package com.example.secretnotev02.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.example.secretnotes.scripts.AES.AES
import com.example.secretnotes.scripts.Pref
import com.example.secretnotes.scripts.sha256
import com.example.secretnotes.scripts.sha512
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.databinding.FragmentLoginBinding
import com.example.secretnotev02.scripts.AppData


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private var callingFunction: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        callingFunction = arguments?.getString("calling_function")
        binding = FragmentLoginBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity

        //Получение хэш пароля
        val prefHashPass = Pref(view.context).getValue("hashPass","")

        //обработка входа
        binding.apply {
            BtnEnter.setOnClickListener {
                val pass = ETPass.text.toString().trim()

                if(pass == "") Toast.makeText(view.context,"Введите пароль", Toast.LENGTH_SHORT).show()
                else
                {
                    //получение хэша пароля и сравниваем с тем что в памяти
                    val hashPass = sha512(pass)
                    if(hashPass == prefHashPass)
                    {
                        //Создание обьекта для шифрования
                        AppData.AES = AES(sha256(pass))
                        AppData.isLogin = true

                        //проверка от куда был вызван фрагмент
                        if (callingFunction == null)
                        {
                            //из главной страницы
                            mainActivity.loadFragment(SecretNotesFragment())
                        }
                        else if (callingFunction == "NotesFragment")
                        {
                            //из фрагмента NotesFragment
                            //Возвращается обратно в фрагмет с кодом 200
                            val resulte = bundleOf("kode" to "200")
                            parentFragmentManager.setFragmentResult("request",resulte)
                            parentFragmentManager.popBackStack()
                        }



                    }

                    else
                        Toast.makeText(view.context, "Не верный пароль", Toast.LENGTH_SHORT).show()


                }

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}