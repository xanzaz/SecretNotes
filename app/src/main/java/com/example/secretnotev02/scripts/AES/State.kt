package com.example.secretnotes.scripts.AES

import java.nio.charset.Charset

class State(
    stateList: List<GF>,
    val Nb: Int,
    val Nr: Int
    ) {
    var state: MutableList<MutableList<GF>> = MutableList(4){ MutableList<GF>(4){  GF(32u) } }

    init
    {
        var index:Int = 0
        for (gf in stateList)
        {
            val col: Int = index % 4
            val row: Int = index / 4

            state[row][col]=gf

            index++
        }
    }

    fun stateToStr(): String
    {
        val charset = Charset.forName("Windows-1251")
        var ubyteArr = mutableListOf<Byte>()
        for (i in 0 until 4)
        {
            for(j in 0 until 4)
            {
                ubyteArr.add(state[i][j].data.toByte())
            }
        }
        val str = String(ubyteArr.toByteArray(),charset)
        return str
    }

    fun stateToListBute(): List<Byte>
    {
        var ubyteArr = mutableListOf<Byte>()
        for (i in 0 until 4)
        {
            for(j in 0 until 4)
            {
                ubyteArr.add(state[i][j].data.toByte())
            }
        }

        return ubyteArr
    }

    fun AddRoundKey(key:List<List<UByte>>,round: Int = 0)
    {
        for (row in 0 until 4)
        {
            for(col in 0 until 4)
            {
                state[row][col] = state[row][col]+key[row][Nb*round+col]
            }
        }
    }

    fun SubBytes(inv: Boolean = false)
    {
        var box: List<List<UByte>>
        if (inv)
        {
            box = SBoxInv
        }
        else
        {
            box = SBox
        }
        for (row in 0 until 4)
        {
            for(col in 0 until 4)
            {
                val i = (state[row][col].data / 0x10u).toInt()
                val j = (state[row][col].data % 0x10u).toInt()
                state[row][col] = GF(box[i][j])
            }
        }

    }

    fun left_shift(stateList: List<GF> , count: Int): MutableList<GF>
    {
        var newB = mutableListOf<GF>()
        newB.addAll(stateList.subList(count,stateList.size))
        newB.addAll(stateList.subList(0,count))
        return newB

    }

    fun right_shift(stateList: List<GF>,count: Int): MutableList<GF>
    {
        var newB = mutableListOf<GF>()
        newB.addAll(stateList.subList(stateList.size-count,stateList.size))
        newB.addAll(stateList.subList(0,stateList.size-count))
        return newB

    }

    fun ShiftRows(inv: Boolean = false)
    {
        if(inv)
        {
            for (i in 1 until 4)
                state[i] = right_shift(state[i],i)
        }
        else
        {
            for (i in 1 until 4)
                state[i] = left_shift(state[i],i)
        }
    }

    fun MixColums(inv:Boolean = false) {
        var resulte: GF = GF(0u)
        var newState = MutableList(4){ MutableList<GF>(4){  GF(0u) } }
        var arr: List<List<UByte>>
        if (inv) arr = FixedArrInv
        else arr = FixedArr

        for (col in 0 until 4)
        {
            for (row in 0 until 4)
            {
                for (j in 0 until 4)
                {
                    val s = state[j][col]
                    val r = arr[row][j]
                    var res = state[j][col] * arr[row][j]
                    resulte = resulte + (state[j][col] * arr[row][j])
                }
                newState[row][col] = resulte
                resulte = GF(0u)
            }
        }
        state = newState


    }

    fun encoding(key: List<List<UByte>>)
    {
        AddRoundKey(key)

        for (rnd in 1 until Nr){
            SubBytes()
            ShiftRows()
            MixColums()
            AddRoundKey(key,rnd)
        }

        SubBytes()
        ShiftRows()
        AddRoundKey(key,Nr)



    }

    fun decrypt(key: List<List<UByte>>)
    {
        AddRoundKey(key,Nr)

        var rnd:Int = Nr - 1
        while (rnd >=1)
        {
            ShiftRows(inv = true)
            SubBytes(inv = true)
            AddRoundKey(key,rnd)
            MixColums(inv = true)

            rnd -=1
        }

        ShiftRows(inv = true)
        SubBytes(inv = true)
        AddRoundKey(key,rnd)
    }

}