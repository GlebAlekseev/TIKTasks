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
import compose.MainWrapper
import compose.MatrixBlock
import compose.ScrollableBox
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


@Composable
@Preview
fun App() {
    // Observe
    val valueK by Calculation.valueK.collectAsState()
    val valueN by Calculation.valueN.collectAsState()
    val valueP by Calculation.valueP.collectAsState()
    val matrixH by Calculation.matrixH.collectAsState()
    val informationPartMessage by Calculation.informationPartMessage.collectAsState()
    val verificationPartMessage by Calculation.verificationPartMessage.collectAsState()
    val syndromeReceive by Calculation.syndromeReceive.collectAsState()

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
        CoroutineScope(Dispatchers.Main).launch {
            Calculation.informationPartMessage.collect {
                val startText = Calculation.informationPartMessage.value.joinToString("") +
                        Calculation.verificationPartMessage.value.joinToString("")
                textFieldReceiveMessage = TextFieldValue(startText)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            Calculation.fullMessageReceive.collect {
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
            Column(
                modifier = Modifier
                    .widthIn(0.dp, 400.dp)
                    .padding(10.dp)
            ) {
                Text("Проверочная матрица:", modifier = Modifier.padding(bottom = 20.dp))
                ScrollableBox(
                    modifier = Modifier
                        .heightIn(0.dp, 200.dp),
                ) {
                    MatrixBlock(matrixH.map { it.toTypedArray() }.toTypedArray(), "b", "a", syndrome =  syndromeReceive)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Сгенерированное сообщение+проверочные разряды:")
            Row {
                Text(
                    informationPartMessage.joinToString(""), modifier = Modifier
                        .background(Color.Gray),
                    color = Color.White
                )
                Text(
                    verificationPartMessage.joinToString(""), modifier = Modifier
                        .background(Color.Gray),
                    color = Color.Green
                )
            }
            Text("Полученное сообщение:")
            Row {
                if(receiveMessageIntArray.isNotEmpty()){
                    fun getErrorPosition(withoutError: String, mbWithError: String): Int?{
                        for (j in withoutError.indices){
                            if (withoutError[j] != mbWithError[j]) return j
                        }
                        return null
                    }
                    @Composable
                    fun Default(information: String, verify: String){
                        Text(
                            information, modifier = Modifier
                                .background(Color.Gray),
                            color = Color.White
                        )
                        Text(
                            verify, modifier = Modifier
                                .background(Color.Gray),
                            color = Color.Green
                        )
                    }
                    runBlocking {
                        jobSetValueK?.join()
                    }
                    var information = ""
                    var verify = ""
                    var withoutError = ""
                    var mbWithError = ""
                    if (receiveMessageIntArray.size > valueK && valueN == valueK + valueP){
                        information = receiveMessageIntArray.slice(0 until valueK).joinToString("")
                        verify = receiveMessageIntArray.slice(valueK until valueN).joinToString("")
                        withoutError = informationPartMessage.joinToString("") + verificationPartMessage.joinToString("")
                        mbWithError = textFieldReceiveMessage.text
                    }else{
                        return@Row
                    }
                    if (mbWithError.length == withoutError.length){
                        val badPosition = getErrorPosition(withoutError, mbWithError)
                        if (badPosition != null){
                            Column {
                                Text(buildAnnotatedString {
                                    for (j in mbWithError.indices){
                                        append(
                                            if (j == badPosition){
                                                AnnotatedString(mbWithError[j].toString(), spanStyle = SpanStyle(Color.Red))
                                            }else if(j < valueK){
                                                AnnotatedString(mbWithError[j].toString(), spanStyle = SpanStyle(Color.White))
                                            }else{
                                                AnnotatedString(mbWithError[j].toString(), spanStyle = SpanStyle(Color.Green))
                                            }
                                        )
                                    }
                                }, modifier = Modifier
                                    .background(Color.Gray)
                                )
                                Text("Позиция ошибки: ${badPosition.let { it + 1 }}", modifier = Modifier.padding(top = 15.dp))
                            }
                        }else{
                            Default(information, verify)
                        }
                    }else{
                        Default(information, verify)
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
                    textFieldReceiveMessage = it
                    isErrorTFReceiveMessage = with(textFieldReceiveMessage.text) {
                        val example = informationPartMessage.joinToString("") + verificationPartMessage.joinToString("")
                        fun countNotMatch(a: String, b: String): Int {
                            var counter = 0
                            for (l in a.indices) {
                                if (a[l] != b[l]) counter++
                            }
                            return counter
                        }
                        toDoubleOrNull() == null || example.length != this.length || !this.contains(Regex("^[10]+$"))
                                || countNotMatch(example, this) > 1
                    }
                    if (!isErrorTFReceiveMessage) {
                        jobSetReceiveMessage?.cancel()
                        jobSetReceiveMessage = CoroutineScope(Dispatchers.IO).launch {
                            Calculation.setFullMessageReceive(textFieldReceiveMessage.text.toList().map { it.code % 2 }
                                .toIntArray(), matrixH)
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
            Text("Синдром: ${syndromeReceive.joinToString("")}")
        }
    }
    setStartTextFieldReceiveMessage()
}

