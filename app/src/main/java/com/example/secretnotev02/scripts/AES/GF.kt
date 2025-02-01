package com.example.secretnotes.scripts.AES

class GF(var data:UByte)
{



    override fun toString(): String {
        return data.toString()
    }
    operator fun plus(uByte: UByte): GF{
        return GF(data xor uByte)
    }
    operator fun plus(gf: GF): GF{
        return GF(data xor gf.data)
    }

    operator fun times(gf: GF): GF
    {
        return GF(gfMultiply(this.data.toInt(),gf.data.toInt()).toUByte())
    }

    operator fun times(uByte: UByte): GF
    {
        return GF(gfMultiply(this.data.toInt(),uByte.toInt()).toUByte())
    }

    fun gfMultiply(a: Int, b: Int): Int {
        var p = 0 // Результат умножения
        var aVar = a
        var bVar = b

        for (i in 0 until 8) {
            if (bVar and 1 != 0) { // Если младший бит bVar равен 1
                p = p xor aVar // Добавляем aVar к результату
            }
            val carry = aVar and 0x80 // Проверяем старший бит aVar
            aVar = aVar shl 1 // Сдвигаем aVar влево на 1 бит
            if (carry != 0) { // Если был перенос
                aVar = aVar xor 0x1B // Выполняем XOR с неприводимым полиномом (0x1B = x^8 + x^4 + x^3 + x + 1)
            }
            bVar = bVar shr 1 // Сдвигаем bVar вправо на 1 бит
        }

        return p and 0xFF // Ограничиваем результат 8 битами
    }



    fun hex():String{
        return data.toString(16)
    }

}