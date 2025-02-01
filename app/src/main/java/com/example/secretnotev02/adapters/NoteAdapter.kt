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
import java.nio.charset.Charset

class NoteAdapter(private var notesTable: MutableList<NoteTable>)
    : RecyclerView.Adapter<NoteAdapter.MyViewHolder>()  {



    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = NoteItemRvBinding.bind(view)

        fun bind(noteTable: NoteTable) = with(binding)
        {
            titleNote.text = noteTable.title
            contentNote.text = noteTable.content
            dateNote.text = noteTable.date
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
        //Заполнение полей
        holder.bind(notesTable[position])

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

    //очищение всего списка
    @SuppressLint("NotifyDataSetChanged")
    fun clearAllActived() {
        for (note in notesTable)
            note.isActive = false
        notifyDataSetChanged()
    }

}