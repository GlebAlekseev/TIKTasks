import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.random.Random

object Calculation {
    private val random = Random(LocalDateTime.now().nano)
    private val _valueN = MutableStateFlow(1)
    val valueN: StateFlow<Int>
        get() = _valueN

    suspend fun setValueN(value: Int) {
        _valueN.value = value
        // A) сгенерировать массив вероятностей появления совокупности дискретных
        // сообщений на входе информационного устройства (P(X));

        val arrayA = Array(value) { random.nextInt(0, Int.MAX_VALUE).toDouble() }

        val k = 1 / arrayA.sum()
        for (i in arrayA.indices) {
            arrayA[i] *= k
        }
        arrayA[arrayA.size - 1] += 1 - arrayA.sum()

        _arrayA.value = arrayA

        // B) cгенерировать матрицу вероятностей перехода со входа на выход (P(X/Y));

        val matrixB = Array(value) {
            Array(value) {
                random.nextInt(0, Int.MAX_VALUE).toDouble()
            }
        }
        for (i in matrixB.indices) {
            matrixB[i][i] = random.nextDouble(0.7, 1.0)
        }

        for (i in matrixB.indices) {
            var sum = 0.0
            for (j in matrixB[i].indices){
                if (j != i){
                    sum+=matrixB[i][j]
                }
            }
            val k = (1-matrixB[i][i]) / sum

            for (j in matrixB[i].indices) {
                if (j!=i){
                    matrixB[i][j] *= k
                }
            }
            matrixB[i][matrixB[i].size - 1] += 1 - matrixB[i].sum()
        }
//        for (i in matrixB.indices){
//            println(matrixB[i].sum())
//        }

        _matrixB.value = matrixB

        // C) рассчитать вероятности появления совокупности дискретных сообщений на
        // выходе информационного устройства (P(Y));
        val arrayC = Array(value) { 0.0 }
        for (i in arrayC.indices) {
            arrayC[i] = getPYi(i)
        }

        _arrayC.value = arrayC

        // D) рассчитать матрицу вероятностей совместных событий (P(X,Y));
        val matrixD = Array(value) {
            Array(value) {
                0.0
            }
        }
        for (i in matrixD.indices) {
            for (j in matrixD.indices) {
                matrixD[i][j] = getPxy(i, j)
            }
        }

        _matrixD.value = matrixD

        // E) определить энтропию на входе информационного устройства (H(X));

        val entropy = getEntropy()
        _entropy.value = entropy

        // F) определить остаточную или условную энтропию выходного сообщения
        //относительно входного (H(Х/Y));

        val conditionalEntropy = getConditionalEntropy()
        _conditionalEntropy.value = conditionalEntropy

        // H) определить количество информации при неполной достоверности сообщений
        //(I(X,Y)).
        val averageI = getAverageI()
        _averageI.value = averageI

    }

    suspend fun repeat() {
        setValueN(valueN.value)
    }

    private fun getPYi(index: Int): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            sum += arrayA.value[i] * matrixB.value[i][index]
        }
        return sum
    }

    private fun getPxy(i: Int, j: Int): Double {
        return arrayC.value[j] * matrixB.value[i][j]
    }

    private fun getEntropy(): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            sum += arrayA.value[i] * log2(arrayA.value[i])
        }
        return -1 * sum
    }

    private fun getConditionalEntropy(): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            for (j in arrayA.value.indices) {
                sum += matrixD.value[i][j] * log2(matrixB.value[i][j])
            }
        }
        return -1 * sum
    }

    private fun getAverageI(): Double {
        return entropy.value - conditionalEntropy.value
    }


    private var _arrayA = MutableStateFlow(Array(0) { 0.0 })
    val arrayA: StateFlow<Array<Double>>
        get() = _arrayA

    private var _matrixB = MutableStateFlow(Array(0) { Array(0) { 0.0 } })
    val matrixB: StateFlow<Array<Array<Double>>>
        get() = _matrixB

    private var _arrayC = MutableStateFlow(Array(0) { 0.0 })
    val arrayC: StateFlow<Array<Double>>
        get() = _arrayC

    private var _matrixD = MutableStateFlow(Array(0) { Array(0) { 0.0 } })
    val matrixD: StateFlow<Array<Array<Double>>>
        get() = _matrixD

    private var _entropy = MutableStateFlow(0.0)
    val entropy: StateFlow<Double>
        get() = _entropy

    private var _conditionalEntropy = MutableStateFlow(0.0)
    val conditionalEntropy: StateFlow<Double>
        get() = _conditionalEntropy

    private var _averageI = MutableStateFlow(0.0)
    val averageI: StateFlow<Double>
        get() = _averageI


}