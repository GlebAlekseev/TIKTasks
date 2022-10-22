import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.math.log2
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

        // Получить все чексуммы
        _checksums.value = getChecksums(informationPartMessage.value, valueP.value)

        // Получить информационные + проверочные разряды
        _informationWithVerifyMessage.value =
            getInformationWithVerifyMessage(informationPartMessage.value, valueN.value, checksums.value)

        // Двукратные ошибки
        _doubleErrorsInformationWithVerifyMessage.value =
            getDoubleErrorsInformationWithVerifyMessage(informationWithVerifyMessage.value)

        setFullMessageReceive(doubleErrorsInformationWithVerifyMessage.value)
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

    private fun getInformationPartMessage(k: Int): IntArray {
        return IntArray(k) { random.nextBits(1) }
    }

    private fun getChecksums(informationPart: IntArray, p: Int): IntArray {
        val result = IntArray(p)

        var infoIndex = 0
        for (i in 0 until (informationPart.size + p)) {
            val res = log2(i.toDouble() + 1)
            if (res - res.toInt() != 0.0) {
                val binaryArray = Integer.toBinaryString(i + 1).map { it.code % 2 }.toIntArray().reversedArray()

                binaryArray.forEachIndexed { index, binary ->
                    if (binary == 1) {
                        result[index] = (result[index] + informationPart[infoIndex]) % 2
                    }
                }
                infoIndex++
            }
        }

        for (i in informationPart.indices) {
            val binaryArray = Integer.toBinaryString(i).map { it.code % 2 }.toIntArray().reversedArray()
            binaryArray.forEachIndexed { index, binary ->
                result[index] = (result[index] + binary) % 2
            }
        }
        return result
    }

    private fun getInformationWithVerifyMessage(informationPart: IntArray, n: Int, checksums: IntArray): IntArray {
        var bIndex = 0
        var informationIndex = 0
        val result = IntArray(n)
        for (i in 0 until n) {
            if (i + 1 == Math.pow(2.0, bIndex.toDouble()).toInt()) {
                val checksum = checksums[bIndex]
                if (checksum == 1) {
                    result[i] = 1
                } else {
                    result[i] = 0
                }
                bIndex++
            } else {
                result[i] = informationPart[informationIndex]
                informationIndex++
            }
        }
        return result
    }

    private fun getDoubleErrorsInformationWithVerifyMessage(informationWithVerifyMessage: IntArray): IntArray {
        val result = IntArray(informationWithVerifyMessage.size + 1)
        informationWithVerifyMessage.forEachIndexed { index, int ->
            result[index] = int
        }
        result[informationWithVerifyMessage.size] = informationWithVerifyMessage.sum() % 2
        return result
    }

    fun setFullMessageReceive(doubleErrorsInformationWithVerifyMessage: IntArray) {
        _doubleErrorsInformationWithVerifyMessageReceive.value = doubleErrorsInformationWithVerifyMessage

        // Последняя контрольная сумма
        val lastChecksum = doubleErrorsInformationWithVerifyMessage.sum() % 2

        // Получаю информационную часть
        val informationPart = getInformationPartFromReceive(doubleErrorsInformationWithVerifyMessageReceive.value)

        // Получаю чексуммы
        val checksums = getChecksums(informationPart, valueP.value)

        // Получаю Синдром
        val syndromes =
            getSyndromes(valueP.value, checksums, doubleErrorsInformationWithVerifyMessageReceive.value).reversedArray()
        _syndromes.value = syndromes

        // Вынесение вердикта
        if (lastChecksum == 0) {
            if (syndromes.filter { it == 1 }.isEmpty()) {
                // Без ошибок
                _positionError.value = -1
            } else {
                // Есть двойная ошибка
                _positionError.value = -2
            }
        } else {
            // Есть одна ошибка
            // Получаю позицию на основе синдрома
            val position = syndromes.joinToString("").toInt(2)
            _positionError.value = position
        }
    }

    private fun getInformationPartFromReceive(receiveMessage: IntArray): IntArray {
        val result = IntArray(valueK.value)
        var resultIndex = 0
        receiveMessage.forEachIndexed { index, int ->
            if (index != receiveMessage.size - 1) {
                val res = log2(index.toDouble() + 1)
                if (res - res.toInt() != 0.0) {
                    result[resultIndex] = receiveMessage[index]
                    resultIndex++
                }
            }
        }
        return result
    }

    private fun getSyndromes(p: Int, checksums: IntArray, messageReceive: IntArray): IntArray {
        val result = IntArray(p)
        for (i in 0 until p) {
            val dd = Math.pow(2.0, i.toDouble()).toInt()
            result[i] = (checksums[i] + messageReceive[dd - 1]) % 2
        }
        return result
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

    private var _checksums = MutableStateFlow(IntArray(0))
    val checksums: StateFlow<IntArray>
        get() = _checksums

    private var _informationWithVerifyMessage = MutableStateFlow(IntArray(0))
    val informationWithVerifyMessage: StateFlow<IntArray>
        get() = _informationWithVerifyMessage

    private var _doubleErrorsInformationWithVerifyMessage = MutableStateFlow(IntArray(0))
    val doubleErrorsInformationWithVerifyMessage: StateFlow<IntArray>
        get() = _doubleErrorsInformationWithVerifyMessage

    private var _doubleErrorsInformationWithVerifyMessageReceive = MutableStateFlow(IntArray(0))
    val doubleErrorsInformationWithVerifyMessageReceive: StateFlow<IntArray>
        get() = _doubleErrorsInformationWithVerifyMessageReceive

    private var _syndromes = MutableStateFlow(IntArray(0))
    val syndromes: StateFlow<IntArray>
        get() = _syndromes

    private var _positionError = MutableStateFlow(-1)
    val positionError: StateFlow<Int>
        get() = _positionError

}