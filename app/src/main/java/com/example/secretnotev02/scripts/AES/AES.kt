package com.example.secretnotes.scripts.AES

import java.nio.charset.Charset

class AES(key: String) {

    var Keys = transformTextToArrKey(key)
//    val Nb = 4  // Количество столбцов составляющие State
//    val Nk = 6  // Количество столбцов ключа шифрования
//    val Nr = 12 // Количество итераций шифрования

    //Преобрабование текста в массив байтов
    fun String.toUByteArray(): List<UByte> {

        val charset = Charset.forName("windows-1251") // Указываем кодировку Windows-1251
        return this.toByteArray(charset).map { it.toUByte() }
    }

    fun updateKeys(hashNewPass: String)
    {
        Keys = transformTextToArrKey(hashNewPass)
    }

    //Метод создания массива state из текста
    fun transformTextToStates(text: String): MutableList<State>
    {
        val arrayGF: MutableList<GF> = mutableListOf()
        val states: MutableList<State> = mutableListOf()

        for (uByte in text.toUByteArray())
        {
            arrayGF.add(GF(uByte))
        }
        val arrChunks = arrayGF.chunked(16)

        for (chunk in arrChunks)
        {
            states.add(State(chunk))
        }

        return states
    }

    fun SubWord(tmp: MutableList<UByte>): MutableList<UByte>
    {
        for (j in 0 until tmp.size)
        {
            val row = (tmp[j] / 0x10u).toInt()
            val col = (tmp[j] % 0x10u).toInt()
            tmp[j] = SBox[row][col]
        }
        return tmp
    }

    //Метод создания масива паролей
    fun transformTextToArrKey(text: String): List<List<UByte>>
    {
        var tmpKey = MutableList(4){ MutableList<UByte>(Nk) {0u} }
        val listUByte= text.toUByteArray().chunked(16).first()

        // Заолнение матрицы данными
        var index:Int = 0
        for (uByte in listUByte)
        {
            val col: Int = index % Nk
            val row: Int = index / Nk

            tmpKey[row][col]=uByte

            index++
        }



        // Цикт создания ключей
        for (col in Nk until Nb*(Nr+1))
        {
            if (col % Nk == 0)
            {
                var tmp  = mutableListOf<UByte>()
                for (row in 1 until 4) tmp.add(tmpKey[row][col-1])
                tmp.add(tmpKey[0][col-1])

                tmp = SubWord(tmp)

                for (row in 0 until 4)
                {
                    val s = tmpKey[row][col- Nk] xor tmp[row] xor RCon[row][col/Nk -1].toUByte()
                    tmpKey[row].add(s)
                }
            }
            else if ((Nk > 6) && (col % Nk == 4))
            {
                var tmp  = mutableListOf<UByte>()
                for (row in 0 until 4) tmp.add(tmpKey[row][col-1])
                tmp = SubWord(tmp)
                for (row in 0 until 4)
                {
                    val s = tmpKey[row][col - Nk] xor tmp[row]
                    tmpKey[row].add(s)
                }
            }
            else
            {
                for (row in 0 until 4)
                {
                    val s = tmpKey[row][col - Nk] xor tmpKey[row][col - 1]
                    tmpKey[row].add(s)
                }
            }

        }



        return tmpKey
    }

    fun encodingText(text: String, key: List<List<UByte>> = Keys): ByteArray
    {
        val States = transformTextToStates(text)
        val encListUByte: MutableList<Byte> = mutableListOf()

        for(state in States)
        {
            state.encoding(key)
            encListUByte.addAll(state.stateToListBute())
        }

        return encListUByte.toByteArray()
    }

    fun decryptText(bytes: ByteArray, key: List<List<UByte>> = Keys ): String
    {
        val states = mutableListOf<State>()
        val arrChunks = bytes.toList().map { GF(it.toUByte()) }.chunked(16)

        var str: String = ""

        for (chunk in arrChunks)
        {
            states.add(State(chunk))
        }

        for(state in states)
        {
            state.decrypt(key)
            str += state.stateToStr()
        }

        return str



    }

    fun reEncodingText(byteArray: ByteArray, hashNewPass: String): ByteArray
    {
        val text = decryptText(byteArray)
        val newKeys = transformTextToArrKey(hashNewPass)
        val encByteArr = encodingText(text,newKeys)
        return encByteArr
    }
    



}