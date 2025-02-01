package com.example.secretnotev02.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.secretnotes.scripts.AES.AES
import com.example.secretnotes.scripts.Pref
import com.example.secretnotes.scripts.sha256
import com.example.secretnotes.scripts.sha512
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.databinding.FragmentRegistrationBinding
import com.example.secretnotev02.scripts.AppData


class RegistrationFragment : Fragment() {

    lateinit var bilding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bilding = FragmentRegistrationBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return bilding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = Pref(view.context)

        val mainActivity = requireActivity() as MainActivity

        bilding.apply {
            BtnSave.setOnClickListener {
                val pass = ETPass.text.toString().trim()
                val repeatedPass = ETRepeatedPass.text.toString().trim()
                if(pass == "" || repeatedPass == "")
                {
                    Toast.makeText(view.context, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    if(pass == repeatedPass)
                    {
                        val hashPass = sha512(pass)
                        pref.saveValue("hashPass",hashPass)

                        AppData.AES = AES(sha256(pass))
                        AppData.isLogin = true
                        mainActivity.loadFragment(SecretNotesFragment())
                    }
                    else
                    {
                        Toast.makeText(view.context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() = RegistrationFragment()

    }
}