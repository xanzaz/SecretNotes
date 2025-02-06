package com.example.secretnotev02.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.secretnotev02.R
import com.example.secretnotev02.DB.NoteTable
import com.example.secretnotev02.databinding.NoteItemRvBinding
import com.example.secretnotev02.scripts.highlightMatches
import java.nio.charset.Charset
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class NoteAdapter(private var notesTable: MutableList<NoteTable>)
    : RecyclerView.Adapter<NoteAdapter.MyViewHolder>()  {

    private var currentQuery: String? = null

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = NoteItemRvBinding.bind(view)

        fun bind(
            truncatedTitle: String,
            truncatedContent: String,
            date: String,
            query: String?
        ) = with(binding)
        {
            titleNote.text = highlightMatches(truncatedTitle,query)
            contentNote.text = highlightMatches(truncatedContent,query)
            dateNote.text = date

        }
    }

    //Интерфейс для Интерфейс для обработки событий
    interface OnItemInteractionListener{
        fun onItemClick(position: Int,noteTable: NoteTable, view: View)
        fun onItemLongClick(position: Int,noteTable: NoteTable, view: View)
    }
    private var listener: OnItemInteractionListener? = null

    // Метод для установки слушателя из Activity
    fun setListener(listener: OnItemInteractionListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item_rv,parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notesTable.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val truncatedTitle = truncateAroundMatch(notesTable[position].title,currentQuery,50)
        val truncatedContent = truncateAroundMatch(notesTable[position].content,currentQuery,50)
        val date = notesTable[position].date

        //Заполнение полей
        holder.bind(truncatedTitle,truncatedContent,date, query = currentQuery)

        //обновление поля isActive
        holder.itemView.isActivated = notesTable[position].isActive

        //Обрабртчики нажатия на объект
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position,notesTable[position],holder.itemView)
            notifyItemChanged(position)
        }
        holder.itemView.setOnLongClickListener {
            listener?.onItemLongClick(position,notesTable[position],holder.itemView)
            notifyItemChanged(position)
            true
        }
    }

    // получение заметок из списка
    fun getNotesTable(): MutableList<NoteTable> {
        return notesTable
    }

    //добавление заметки
    @SuppressLint("NotifyDataSetChanged")
    fun addNote(noteTable: NoteTable) {
        notesTable.add(noteTable)
        notifyDataSetChanged()
    }

    //Удаление заметки
    @SuppressLint("NotifyDataSetChanged")
    fun deleteNotes(notesTable_inp: List<NoteTable>) {
        for (note in notesTable_inp)
            notesTable.remove(note)
        notifyDataSetChanged()
    }

    //обнавление данных заметки
    @SuppressLint("NotifyDataSetChanged")
    fun updateNote(position: Int, noteTable: NoteTable) {
        notesTable[position]=noteTable
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateNote(noteTable: NoteTable) {
        val index = notesTable.indexOfFirst { it.id == noteTable.id }
        notesTable[index] = noteTable

        notifyDataSetChanged()
    }

    //обнавляет весь список
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newNotesTable: List<NoteTable>, query: String?)
    {
        notesTable = newNotesTable.toMutableList()
        currentQuery = query
        notifyDataSetChanged()

    }

    //очищение всего списка
    @SuppressLint("NotifyDataSetChanged")
    fun clearAllActived() {
        for (note in notesTable)
            note.isActive = false
        notifyDataSetChanged()
    }

    private fun truncateAroundMatch(fullText: String, query: String?, maxLength: Int = 100): String {
        if (query.isNullOrEmpty()) return fullText

        val lowerText = fullText.lowercase(Locale.getDefault())
        val lowerQuery = query.lowercase(Locale.getDefault())
        val matchIndex = lowerText.indexOf(lowerQuery)

        if (matchIndex == -1) return fullText

        // Вычисляем диапазон обрезки
        val start = max(0, matchIndex - maxLength / 2)
        val end = min(fullText.length, matchIndex + query.length + maxLength / 2)

        val truncated = buildString {
            if (start > 0) append("...")
            append(fullText.substring(start, end))
            if (end < fullText.length) append("...")
        }

        return truncated.replace("\n","")
    }





}