import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import compose.MainWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.log2

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Задание 5", resizable = true) {
        App()
    }
}

@Composable
@Preview
fun App() {
    // Observe
    val valueK by Calculation.valueK.collectAsState()
    val valueN by Calculation.valueN.collectAsState()
    val valueP by Calculation.valueP.collectAsState()
    val informationPartMessage by Calculation.informationPartMessage.collectAsState()
    val informationWithVerifyMessage by Calculation.informationWithVerifyMessage.collectAsState()
    val doubleErrorsInformationWithVerifyMessage by Calculation.doubleErrorsInformationWithVerifyMessage.collectAsState()
    val doubleErrorsInformationWithVerifyMessageReceive by Calculation.doubleErrorsInformationWithVerifyMessageReceive.collectAsState()
    val syndromes by Calculation.syndromes.collectAsState()
    val positionError by Calculation.positionError.collectAsState()


    // Base Settings
    var textFieldK by remember { mutableStateOf(TextFieldValue()) }
    var isErrorTFK by remember { mutableStateOf(false) }

    var textFieldReceiveMessage by remember { mutableStateOf(TextFieldValue()) }
    var isErrorTFReceiveMessage by remember { mutableStateOf(false) }

    var receiveMessageIntArray by remember { mutableStateOf(IntArray(0)) }

    var jobSetValueK: Job? = null
    var jobSetReceiveMessage: Job? = null

    // Init
    fun setStartTextFieldN() {
        val startValue = "50"
        textFieldK = TextFieldValue(startValue)
        jobSetValueK?.cancel()
        jobSetValueK = CoroutineScope(Dispatchers.IO).launch {
            Calculation.setValueK(startValue.toInt())
        }
    }

    fun setStartTextFieldReceiveMessage() {
        jobSetReceiveMessage = CoroutineScope(Dispatchers.Main).launch {
            Calculation.doubleErrorsInformationWithVerifyMessage.collect {
                val startText = Calculation.doubleErrorsInformationWithVerifyMessage.value.joinToString("")
                textFieldReceiveMessage = TextFieldValue(startText)
            }
        }
        jobSetReceiveMessage = CoroutineScope(Dispatchers.Main).launch {
            Calculation.doubleErrorsInformationWithVerifyMessageReceive.collect {
                receiveMessageIntArray = it
            }
        }
    }

    setStartTextFieldN()
    // Program content
    MainWrapper {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(
                singleLine = true,
                value = textFieldK,
                label = {
                    Text("Количество информационных разярдов")
                },
                isError = isErrorTFK,
                onValueChange =
                {
                    textFieldK = it
                    isErrorTFK = with(textFieldK.text) { toIntOrNull() == null || toInt() < 0 || toInt() > 64 }
                    if (!isErrorTFK) {
                        jobSetValueK?.cancel()
                        jobSetValueK = CoroutineScope(Dispatchers.IO).launch {
                            Calculation.setValueK(it.text.toInt())
                        }
                    }
                },
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
            )
            Button(
                onClick = {
                    jobSetValueK?.cancel()
                    jobSetValueK = CoroutineScope(Dispatchers.IO).launch {
                        Calculation.repeat()
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(160.dp)
            ) {
                Text(text = "Повторить")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp), horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(0.dp, 400.dp)
                    .padding(10.dp)
            ) {
                Row {
                    Text(text = "Кол-во информационных разрядов k: ")
                    Text(
                        valueK.toString(), color = Color.White, modifier = Modifier
                            .background(Color.Gray)
                    )
                }
                Row {
                    Text(text = "Кол-во проверочных разрядов p:        ")
                    Text(
                        valueP.toString(), color = Color.White, modifier = Modifier
                            .background(Color.Gray)
                    )
                }
                Row {
                    Text(text = "Кол-во разрядов n=(p+k):                   ")
                    Text(
                        valueN.toString(), color = Color.White, modifier = Modifier
                            .background(Color.Gray)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Сгенерированное сообщение:")
            Row {
                Text(
                    informationPartMessage.joinToString(""), modifier = Modifier
                        .background(Color.Gray),
                    color = Color.White
                )
            }
            Text("Сгенерированное сообщение c проверочными разрядами(1xОшибка):")
            Row {
                Text(
                    buildAnnotatedString {
                        for (j in 0 until informationWithVerifyMessage.size) {
                            val res = log2(j.toDouble() + 1)
                            append(
                                if (res - res.toInt() == 0.0) {
                                    AnnotatedString(informationWithVerifyMessage[j].toString(), SpanStyle(Color.Green))
                                } else {
                                    AnnotatedString(informationWithVerifyMessage[j].toString(), SpanStyle(Color.White))
                                }
                            )
                        }
                    }, modifier = Modifier
                        .background(Color.Gray)
                )
            }
            Text("Сгенерированное сообщение c проверочными разрядами(2xОшибка):")
            Row {
                Text(
                    buildAnnotatedString {
                        for (j in 0 until doubleErrorsInformationWithVerifyMessage.size) {
                            val res = log2(j.toDouble() + 1)
                            append(
                                if (j == doubleErrorsInformationWithVerifyMessage.size - 1) {
                                    AnnotatedString(
                                        doubleErrorsInformationWithVerifyMessage[j].toString(),
                                        SpanStyle(Color.Yellow)
                                    )
                                } else if (res - res.toInt() == 0.0) {
                                    AnnotatedString(
                                        doubleErrorsInformationWithVerifyMessage[j].toString(),
                                        SpanStyle(Color.Green)
                                    )
                                } else {
                                    AnnotatedString(
                                        doubleErrorsInformationWithVerifyMessage[j].toString(),
                                        SpanStyle(Color.White)
                                    )
                                }
                            )
                        }
                    }, modifier = Modifier
                        .background(Color.Gray)
                )

            }
            Row {
                if (doubleErrorsInformationWithVerifyMessageReceive.isNotEmpty()) {
                    fun getErrorPosition(withoutError: String, mbWithError: String): IntArray {
                        val array = mutableListOf<Int>()
                        for (j in withoutError.indices) {
                            if (withoutError[j] != mbWithError[j]) array.add(j)
                        }
                        return array.toIntArray()
                    }

                    var withoutError = ""
                    var mbWithError = ""
                    if (receiveMessageIntArray.size > valueK && valueN == valueK + valueP) {
                        withoutError = doubleErrorsInformationWithVerifyMessage.joinToString("")
                        mbWithError = textFieldReceiveMessage.text
                    } else {
                        return@Row
                    }
                    if (mbWithError.length == withoutError.length) {
                        val badPositions = getErrorPosition(withoutError, mbWithError)
                        if (badPositions.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Сгенерированное сообщение c проверочными разрядами(2xОшибка) + Позиция ошибки:")
                                Text(
                                    buildAnnotatedString {
                                        for (j in 0 until doubleErrorsInformationWithVerifyMessage.size) {
                                            val res = log2(j.toDouble() + 1)
                                            append(
                                                if (badPositions.contains(j)) {
                                                    AnnotatedString(mbWithError[j].toString(), SpanStyle(Color.Red))
                                                } else if (j == doubleErrorsInformationWithVerifyMessage.size - 1) {
                                                    AnnotatedString(mbWithError[j].toString(), SpanStyle(Color.Yellow))
                                                } else if (res - res.toInt() == 0.0) {
                                                    AnnotatedString(mbWithError[j].toString(), SpanStyle(Color.Green))
                                                } else {
                                                    AnnotatedString(mbWithError[j].toString(), SpanStyle(Color.White))
                                                }
                                            )
                                        }
                                    }, modifier = Modifier
                                        .background(Color.Gray)
                                )
                            }
                        }
                    }

                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(
                singleLine = true,
                value = textFieldReceiveMessage,
                label = {
                    Text("Полученное сообщение")
                },
                isError = isErrorTFReceiveMessage,
                onValueChange =
                {
                    if (it.text == textFieldReceiveMessage.text) {
                        textFieldReceiveMessage = it
                        return@TextField
                    }
                    textFieldReceiveMessage = it
                    isErrorTFReceiveMessage = with(textFieldReceiveMessage.text) {
                        val example = doubleErrorsInformationWithVerifyMessage.joinToString("")
                        fun countNotMatch(a: String, b: String): Int {
                            var counter = 0
                            for (l in a.indices) {
                                if (a[l] != b[l]) counter++
                            }
                            return counter
                        }
                        toDoubleOrNull() == null || example.length != this.length || !this.contains(Regex("^[10]+$"))
                                || countNotMatch(example, this) > 2
                    }
                    if (!isErrorTFReceiveMessage) {
                        jobSetReceiveMessage?.cancel()
                        jobSetReceiveMessage = CoroutineScope(Dispatchers.IO).launch {
                            Calculation.setFullMessageReceive(textFieldReceiveMessage.text.toList().map { it.code % 2 }
                                .toIntArray())
                        }
                    }
                },
                modifier = Modifier
                    .height(60.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (positionError == -2) {
                Text("Обнаружено две ошибки")
            } else if (positionError == -1) {
                Text("Ошибок нет")
            } else {
                Column {
                    Text("Обнаружена одна ошибка")
                    Text("Синдром: ${syndromes.joinToString("")}")
                    Text("Позиция: ${positionError}")
                    val correctedText = textFieldReceiveMessage.text.map { it.code % 2 }.toIntArray()
                    correctedText[positionError - 1] = (correctedText[positionError - 1] + 1) % 2
                    Text(
                        buildAnnotatedString {
                            append(
                                AnnotatedString("Исправлено:\n", SpanStyle(Color.White))
                            )
                            correctedText.forEachIndexed { index, int ->
                                if (index == positionError - 1) {
                                    append(
                                        AnnotatedString(int.toString(), SpanStyle(Color.Green))
                                    )
                                } else {
                                    append(
                                        AnnotatedString(int.toString(), SpanStyle(Color.White))
                                    )
                                }
                            }
                        }, modifier = Modifier
                            .background(Color.Gray)
                    )
                }
            }
        }
    }
    setStartTextFieldReceiveMessage()
}

