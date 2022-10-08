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

    fun setValueN(value: Int) {
        _valueN.value = value

        // A) сгенерировать массив вероятностей появления совокупности сообщений на
        // входе дискретного канала; P(Y)

        val arrayA = Array(value) { random.nextInt(0, Int.MAX_VALUE).toDouble() }

        val k = 1 / arrayA.sum()
        for (i in arrayA.indices) {
            arrayA[i] *= k
        }
        arrayA[arrayA.size - 1] += 1 - arrayA.sum()

        _arrayA.value = arrayA

        // Энтропия без помех H(Y)

        _entropy.value = getEntropy()

        // B) сгенерировать длительности каждого символа сообщения;

        val arrayB = Array(value) { random.nextInt(1, value + 1) }
        _arrayB.value = arrayB

        // средняя длительность элементарных сигналов.
        var averageDurationOfElementarySignals = 0.0
        for (i in 0 until value){
            averageDurationOfElementarySignals += arrayA[i]*arrayB[i]
        }
        // мс -> с
        _averageDurationOfElementarySignals.value = averageDurationOfElementarySignals/1000

        // скорость передачи элементарных символов сигнала
        _transmissionRateOfElementarySignalSymbols.value = getTransmissionRateOfElementarySignalSymbols()

        // C) сгенерировать матрицу переходов со входа на выход в канале передачи
        // информации с помехами с учетом технического задания, используя счетчик
        // случайных чисел;

        val matrixC = Array(value) {
            Array(value) {
                1.0 / (2.0*value)
            }
        }
        for (i in matrixC.indices) {
            matrixC[i][i] = 1.0 - (matrixC[i].sum() - matrixC[i][i])
        }
//        for (i in matrixC.indices){
//            println(matrixC[i].sum())
//        }
        _matrixC.value = matrixC

        // рассчитать вероятности появления совокупности дискретных сообщений на
        // выходе информационного устройства (P(Y));
        val arrayC = Array(value) { 0.0 }
        for (i in arrayC.indices) {
            arrayC[i] = getPYi(i)
        }
        _arrayC.value = arrayC

        // рассчитать матрицу вероятностей совместных событий (P(Y,Z));
        val matrixC2 = Array(value) {
            Array(value) {
                0.0
            }
        }
        for (i in matrixC2.indices) {
            for (j in matrixC2.indices) {
                matrixC2[i][j] = getPxy(i, j)
            }
        }
        _matrixC2.value = matrixC2

        // Условная энтропия H(Y,Z)
        _conditionalEntropy.value = getConditionalEntropy()

        // D) рассчитать пропускную способность и скорость передачи при использовании
        // канала без помех;

        _bandwidthWithoutInterference.value = getBandwidthWithoutInterference(value.toDouble())

        _transferRateWithoutInterference.value = getTransferRateWithoutInterference()

        // E) рассчитать пропускную способность и скорость передачи при использовании
        // канала с помехами.

        _bandwidthWithInterference.value = getBandwidthWithInterference(value.toDouble())

        _transferRateWithInterference.value = getTransferRateWithInterference()
    }

    private fun getEntropy(): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            sum += arrayA.value[i] * log2(arrayA.value[i])
        }
        return -1 * sum
    }
    private fun getPYi(index: Int): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            sum += arrayA.value[i] * matrixC.value[i][index]
        }
        return sum
    }
    private fun getPxy(i: Int, j: Int): Double {
        return arrayC.value[j] * matrixC.value[i][j]
    }
    private fun getConditionalEntropy(): Double {
        var sum = 0.0
        for (i in arrayA.value.indices) {
            for (j in arrayA.value.indices) {
                sum += matrixC2.value[i][j] * log2(matrixC.value[i][j])
            }
        }
        return -1 * sum
    }

    private fun getTransmissionRateOfElementarySignalSymbols(): Double{
        return 1 / averageDurationOfElementarySignals.value
    }

    private fun getBandwidthWithoutInterference(N: Double): Double{
        return transmissionRateOfElementarySignalSymbols.value * log2(N)
    }
    private fun getBandwidthWithInterference(N: Double): Double{
        return transmissionRateOfElementarySignalSymbols.value * (log2(N) - conditionalEntropy.value)
    }
    private fun getTransferRateWithoutInterference(): Double{
        return transmissionRateOfElementarySignalSymbols.value * entropy.value
    }
    private fun getTransferRateWithInterference(): Double{
        return transmissionRateOfElementarySignalSymbols.value * (entropy.value - conditionalEntropy.value)
    }

    suspend fun repeat() {
        setValueN(valueN.value)
    }

    private var _arrayA = MutableStateFlow(Array(0) { 0.0 })
    val arrayA: StateFlow<Array<Double>>
        get() = _arrayA

    private var _arrayB = MutableStateFlow(Array(0) { 0 })
    val arrayB: StateFlow<Array<Int>>
        get() = _arrayB

    private var _matrixC = MutableStateFlow(Array(0) { Array(0) { 0.0 } })
    val matrixC: StateFlow<Array<Array<Double>>>
        get() = _matrixC

    private var _matrixC2 = MutableStateFlow(Array(0) { Array(0) { 0.0 } })
    val matrixC2: StateFlow<Array<Array<Double>>>
        get() = _matrixC2

    private var _arrayC = MutableStateFlow(Array(0) { 0.0 })
    val arrayC: StateFlow<Array<Double>>
        get() = _arrayC

    private var _entropy = MutableStateFlow(0.0)
    val entropy: StateFlow<Double>
        get() = _entropy

    private var _conditionalEntropy = MutableStateFlow(0.0)
    val conditionalEntropy: StateFlow<Double>
        get() = _conditionalEntropy

    private var _averageDurationOfElementarySignals = MutableStateFlow(0.0)
    val averageDurationOfElementarySignals: StateFlow<Double>
        get() = _averageDurationOfElementarySignals

    private var _transmissionRateOfElementarySignalSymbols = MutableStateFlow(0.0)
    val transmissionRateOfElementarySignalSymbols: StateFlow<Double>
        get() = _transmissionRateOfElementarySignalSymbols

    private var _bandwidthWithoutInterference = MutableStateFlow(0.0)
    val bandwidthWithoutInterference: StateFlow<Double>
        get() = _bandwidthWithoutInterference

    private var _bandwidthWithInterference = MutableStateFlow(0.0)
    val bandwidthWithInterference: StateFlow<Double>
        get() = _bandwidthWithInterference

    private var _transferRateWithoutInterference = MutableStateFlow(0.0)
    val transferRateWithoutInterference: StateFlow<Double>
        get() = _transferRateWithoutInterference

    private var _transferRateWithInterference = MutableStateFlow(0.0)
    val transferRateWithInterference: StateFlow<Double>
        get() = _transferRateWithInterference
}