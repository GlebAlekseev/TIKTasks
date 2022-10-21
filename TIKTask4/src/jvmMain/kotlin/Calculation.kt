import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import kotlin.random.Random

object Calculation {
    private val random = Random(LocalDateTime.now().nano)
    private val _valueK = MutableStateFlow(1)
    val valueK: StateFlow<Int>
        get() = _valueK

    fun setValueK(value: Int) {
        _valueK.value = value
        println("setValueK")

        // Вычислить n - длина кодовой комбинации
        _valueN.value = getValueNFromK(valueK.value)
        // Проверочная подматрица Hp
        _matrixHp.value = getVerificationMatrix(valueK.value,valueP.value)
        // Производящая матрица Pn,k
        _matrixPnk.value = getMatrixPnk(valueK.value,valueN.value, matrixHp.value)

        // Сгенерировать кодовую комбинацию размером k
        _informationPartMessage.value = getInformationPartMessage(valueK.value)
        // Получение проверочных разрядов
        val verification = matrixMultiply(informationPartMessage.value,matrixPnk.value).slice(valueK.value until valueN.value)
        _verificationPartMessage.value = verification.toIntArray()

        // Проверочная матрица
        _matrixH.value = getMatrixH(matrixHp.value, valueN.value,valueP.value, valueK.value)

        setFullMessageReceive(informationPartMessage.value + verificationPartMessage.value, matrixH.value)
    }

    fun repeat() {
        setValueK(valueK.value)
    }

    private fun getValueNFromK(k: Int): Int{
        val left = Math.pow(2.0, k.toDouble())
        var n = k
        while (n < 1024){
            val right = Math.pow(2.0, n.toDouble())/(1+n)
            if (left <= right) return n
            n++
        }
        throw RuntimeException("bad k=$k")
    }

    fun getVerificationMatrix(k: Int,p: Int): Array<IntArray>{
        println("k=$k p=$p")
        val dmin = 3
        val matrix = Array(k){IntArray(p){0}}
        val minCountOne = dmin-1
        val minStep = dmin-2
        for (i in matrix.indices){
            if (i == 0){
                val startValue = IntArray(p){0}
                var counterOne = minCountOne
                for (j in startValue.size-1 downTo  0){
                    if (counterOne > 0){
                        startValue[j] = 1
                        counterOne--
                    }else{break}
                }
                matrix[i] = startValue
                continue
            }
            var lastValue = matrix[i-1]
            fun checkCount(ints: IntArray): Boolean{
                if (ints.count { it == 1 } >= minCountOne) return true
                return false
            }
            fun IntArray.xor(ints: IntArray): IntArray{
                return IntArray(ints.size).mapIndexed { index, _ ->  this[index] + ints[index] }.toIntArray()
            }
            fun IntArray.inc(): IntArray{
                val result = this.clone()
                var flag = true
                for (j in result.size-1 downTo 0){
                    if (flag){
                        result[j] = (1 + result[j]) % 2
                        flag = result[j] == 0
                    }else break
                }
                return result
            }
            fun checkStep(ints: IntArray): Boolean{
                for (j in matrix.indices){
                    if (i==j) break
                    if (ints.xor(matrix[j]).count { it == 1 } < minStep) return false
                }
                return true
            }
            while (!checkCount(lastValue) || !checkStep(lastValue)){
                lastValue = lastValue.inc()
            }
            matrix[i] = lastValue
        }
        return matrix
    }

    private fun getMatrixPnk(k: Int,n: Int,matrixHp: Array<IntArray>): Array<IntArray>{
        val matrixPnk = Array(k){ IntArray(n){0} }
        for (i in 0 until k){
            matrixPnk[i][i] = 1
        }
        matrixHp.forEachIndexed { indexI, ints ->
            ints.forEachIndexed { indexJ, int ->
                matrixPnk[indexI][k+indexJ] = int
            }
        }
        return matrixPnk
    }

    private fun getInformationPartMessage(k: Int): IntArray{
        return IntArray(k){ random.nextBits(1)}
    }

    private fun getMatrixH(matrixHp: Array<IntArray>, n: Int, p: Int, k: Int): Array<IntArray>{
        val matrixH = Array(n){ IntArray(p){0} }
        matrixHp.forEachIndexed { indexI, ints ->
            ints.forEachIndexed { indexJ, int ->
                matrixH[indexI][indexJ] = int
            }
        }
        var j = 0
        for (i in k until n){
            matrixH[i][j] = 1
            j++
        }
        return matrixH
    }

    fun matrixMultiply(array: IntArray, matrix: Array<IntArray>): IntArray{
        if (matrix.isEmpty() || array.size != matrix.size) throw RuntimeException()
        val newArray = IntArray(matrix[0].size){0}

        for (index in matrix[0].indices){
            var sum = 0
            for (i in array.indices){
                sum += array[i]*matrix[i][index]
            }
            newArray[index] = sum%2
        }
        return newArray
    }

    fun setFullMessageReceive(fullMessage: IntArray, matrixH: Array<IntArray>){
        _fullMessageReceive.value = fullMessage

        // Вычислить Синдром
        val syndrome = matrixMultiply(fullMessage,matrixH)
        _syndromeReceive.value = syndrome
    }



    private var _valueN = MutableStateFlow(0)
    val valueN: StateFlow<Int>
        get() = _valueN

    val valueP: StateFlow<Int>
        get() = valueN.combineState(valueK){ N, K ->
            N-K
        }

    private var _matrixHp = MutableStateFlow(Array(0) { IntArray(0) { 0 } })
    val matrixHp: StateFlow<Array<IntArray>>
        get() = _matrixHp

    private var _matrixPnk = MutableStateFlow(Array(0) { IntArray(0) { 0 } })
    val matrixPnk: StateFlow<Array<IntArray>>
        get() = _matrixPnk

    private var _matrixH = MutableStateFlow(Array(0) { IntArray(0) { 0 } })
    val matrixH: StateFlow<Array<IntArray>>
        get() = _matrixH


    private var _informationPartMessage = MutableStateFlow(IntArray(0))
    val informationPartMessage: StateFlow<IntArray>
        get() = _informationPartMessage

    private var _verificationPartMessage = MutableStateFlow(IntArray(0))
    val verificationPartMessage: StateFlow<IntArray>
        get() = _verificationPartMessage

    private var _fullMessageReceive = MutableStateFlow(IntArray(0))
    val fullMessageReceive: StateFlow<IntArray>
        get() = _fullMessageReceive

    private var _syndromeReceive = MutableStateFlow(IntArray(0))
    val syndromeReceive: StateFlow<IntArray>
        get() = _syndromeReceive

}