package com.example.secretnotev02.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.secretnotev02.MainActivity
import com.example.secretnotev02.R
import com.example.secretnotev02.SettingLayoutActivity
import com.example.secretnotev02.databinding.FragmentSettingListBinding


class SettingListFragment : Fragment() {

    lateinit var binding: FragmentSettingListBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingListBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Создание кнопки назад в toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar_setting))
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {

            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHasOptionsMenu(true)
        }

        val settingActivity = requireActivity() as SettingLayoutActivity



        binding.CVeditPass.setOnClickListener{
            settingActivity.loadFragment(EditPassFragment())
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
}