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

    fun setValueN(value: Int){
        _valueN.value = value
        // Сгенерировать список размером N из рандомных чисел, сумма которых 1
        val array = Array(value){random.nextInt(0,Int.MAX_VALUE).toDouble()}
        val k = 1/array.sum()
        for (i in array.indices){
            array[i] *= k
        }
        array[array.size-1] += 1-array.sum()

        _arrayA.value = array
        // Посчитать и присвоить
        // Среднее количество информации в совокупности сообщений
        _valueI.value = getValueI()
        // Максиамльная энтропия сообщений
        _valueH.value = getValueH()
    }
    fun repeat(){
        setValueN(valueN.value)
    }

    private fun getValueI(): Double{
        var sum = 0.0
        arrayA.value.forEach {
            sum += it*log2(it)
        }
        return -1*sum
    }

    private fun getValueH(): Double{
        return log2(arrayA.value.size.toDouble())
    }

    private var _arrayA = MutableStateFlow(Array(0){0.0})
    val arrayA: StateFlow<Array<Double>>
        get() = _arrayA


    private var _valueI = MutableStateFlow(0.0)
    val valueI: StateFlow<Double>
        get() = _valueI

    private var _valueH = MutableStateFlow(0.0)
    val valueH: StateFlow<Double>
        get() = _valueH
}