import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.random.Random

object Calculation {
    private val random = Random(LocalDateTime.now().nano)
    private val _valueK = MutableStateFlow(1)
    val valueK: StateFlow<Int>
        get() = _valueK

    fun setValueK(value: Int) {
        _valueK.value = value

        // Вычислить n - длина кодовой комбинации
        _valueN.value = getValueNFromK(valueK.value)

        // Сгенерировать кодовую комбинацию размером k
        _informationPartMessage.value = getInformationPartMessage(valueK.value)

        // Получить информационные + проверочные разряды
        _informationWithVerifyMessage.value =
            getInformationWithVerifyMessage(informationPartMessage.value, valueP.value)

        setFullMessageReceive(informationWithVerifyMessage.value)
    }

    fun repeat() {
        setValueK(valueK.value)
    }

    private fun getValueNFromK(k: Int): Int {
        val left = Math.pow(2.0, k.toDouble())
        var n = k
        while (n < 1024) {
            val right = Math.pow(2.0, n.toDouble()) / (1 + n)
            if (left <= right) return n
            n++
        }
        throw RuntimeException("bad k=$k")
    }

    private fun getValueKFromN(n: Int): Int {
        val right =  Math.pow(2.0, n.toDouble()) / (1 + n)
        var k = n
        while (k > 0) {
            val left = Math.pow(2.0, k.toDouble())
            if (left <= right) return k
            k--
        }
        throw RuntimeException("bad n=$n")
    }

    private fun getInformationPartMessage(k: Int): IntArray {
        return IntArray(k) { random.nextBits(1) }
    }

    private fun multiply(informationPartMessage: IntArray, p: Int): IntArray{
        // Сдвинуть каждый не нулевой на p влево
        val position = informationPartMessage.indexOf(1)
        if (position == -1) return IntArray(informationPartMessage.size+p)
        val newArray = IntArray(informationPartMessage.size + p - position){0}
        informationPartMessage.slice(position until informationPartMessage.size).forEachIndexed { index, int ->
            newArray[index] = int
        }
        return newArray
    }

    private fun divide(numerator: IntArray, divider: IntArray, p: Int): IntArray{
        fun IntArray.xor(ints: IntArray): IntArray{
            val result = IntArray(size)
            forEachIndexed { index, int ->
                result[index] = (int + ints[index]) % 2
            }
            return result
        }
        fun hasOne(ints: IntArray): Boolean{
            return ints.indexOf(1) != -1
        }
        fun realSize(ints: IntArray): Int{
            val position = ints.indexOf(1)
            return ints.size - position
        }
        if (!hasOne(divider)) return numerator
        val dividerRealSize = realSize(divider)
        val dividerStartPosition = divider.indexOf(1)
        val newArray = mutableListOf(numerator)
        while (true){
            val lastNewArray = newArray.last()
            val lastNewArrayRealSize = realSize(lastNewArray)
            val lastNewArrayStartPosition = lastNewArray.indexOf(1)
            if (!hasOne(lastNewArray) || lastNewArrayRealSize < dividerRealSize) break

            val remains = IntArray(lastNewArray.size){0}
            var j = 0
            for (i in remains.indices){
                if (i>=lastNewArrayStartPosition && i<lastNewArrayStartPosition+dividerRealSize) {
                    remains[i] = divider[dividerStartPosition + j]
                    j++
                }else{
                    remains[i] = 0
                }
            }
            newArray.add(lastNewArray.xor(remains))
        }
        val result = newArray.last()
        val resultStart = result.size-p
        return result.slice(resultStart until result.size).toIntArray()
    }

    private fun getInformationWithVerifyMessage(informationPartMessage: IntArray, p: Int): IntArray{
        val part1 = multiply(informationPartMessage, p)
        val part2 = divide(part1, irreduciblePolynomials[p-1], p)

        val maxSize = Math.pow(2.0,p.toDouble())-1
        val messageWithPadding = IntArray(maxSize.toInt()){0}
        (informationPartMessage + part2).reversedArray().forEachIndexed { index, int ->
            messageWithPadding[index] = int
        }
        return messageWithPadding.reversedArray()
    }

    fun setFullMessageReceive(informationWithVerifyMessage: IntArray) {
        _informationWithVerifyMessageReceive.value = informationWithVerifyMessage

        // Получить p из сообщения
        val k = getValueKFromN(informationWithVerifyMessage.size)
        val p = informationWithVerifyMessage.size-k
        if (valueP.value != p) throw RuntimeException("bad p")

        // Получить синдром
        _syndromes.value = getSyndromeFromReceiveMessage(informationWithVerifyMessage, p)

        // Получить позицию ошибки
        _positionError.value = getPositionErrorForSyndromes(syndromes.value, informationWithVerifyMessage, p)
    }

    private fun getSyndromeFromReceiveMessage(informationWithVerifyMessageReceive: IntArray, p: Int): IntArray{
        // Вычислить синдром
        return divide(informationWithVerifyMessageReceive, irreduciblePolynomials[p-1], p)
    }

    private fun getPositionErrorForSyndromes(syndromes: IntArray, ints: IntArray, p: Int): Int{
        for (i in ints.indices){
            val intsWithError = IntArray(ints.size){0}
            intsWithError[i] = 1
            val remain = divide(intsWithError, irreduciblePolynomials[p-1], p)
            if (remain.contentEquals(syndromes)) return i+1
        }
        return -1
    }

    private var _valueN = MutableStateFlow(0)
    val valueN: StateFlow<Int>
        get() = _valueN

    val valueP: StateFlow<Int>
        get() = valueN.combineState(valueK) { N, K ->
            N - K
        }

    private var _informationPartMessage = MutableStateFlow(IntArray(0))
    val informationPartMessage: StateFlow<IntArray>
        get() = _informationPartMessage

    private var _informationWithVerifyMessage = MutableStateFlow(IntArray(0))
    val informationWithVerifyMessage: StateFlow<IntArray>
        get() = _informationWithVerifyMessage

    private var _informationWithVerifyMessageReceive = MutableStateFlow(IntArray(0))
    val informationWithVerifyMessageReceive: StateFlow<IntArray>
        get() = _informationWithVerifyMessageReceive

    private var _syndromes = MutableStateFlow(IntArray(0))
    val syndromes: StateFlow<IntArray>
        get() = _syndromes

    private var _positionError = MutableStateFlow(-1)
    val positionError: StateFlow<Int>
        get() = _positionError

    val irreduciblePolynomials = arrayOf(
        intArrayOf(0,0,0,0,0,0,0,1,1),
        intArrayOf(0,0,0,0,0,0,1,1,1),
        intArrayOf(0,0,0,0,0,1,0,1,1),
        intArrayOf(0,0,0,0,1,0,0,1,1),
        intArrayOf(0,0,0,1,0,0,1,0,1),
        intArrayOf(0,0,1,0,0,0,0,1,1),
        intArrayOf(0,1,0,0,0,0,0,1,1),
        intArrayOf(1,0,0,0,1,1,0,1,1),
    )
}