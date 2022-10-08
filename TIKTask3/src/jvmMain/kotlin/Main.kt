// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*


@Composable
@Preview
fun App() {
    val arrayA by Calculation.arrayA.collectAsState()
    val entropy by Calculation.entropy.collectAsState()

    val arrayB by Calculation.arrayB.collectAsState()
    val averageDurationOfElementarySignals by Calculation.averageDurationOfElementarySignals.collectAsState()
    val transmissionRateOfElementarySignalSymbols by Calculation.transmissionRateOfElementarySignalSymbols.collectAsState()

    val matrixC by Calculation.matrixC.collectAsState()
    val matrixC2 by Calculation.matrixC2.collectAsState()
    val arrayC by Calculation.arrayC.collectAsState()
    val conditionalEntropy by Calculation.conditionalEntropy.collectAsState()

    val transferRateWithoutInterference by Calculation.transferRateWithoutInterference.collectAsState()
    val bandwidthWithoutInterference by Calculation.bandwidthWithoutInterference.collectAsState()

    val transferRateWithInterference by Calculation.transferRateWithInterference.collectAsState()
    val bandwidthWithInterference by Calculation.bandwidthWithInterference.collectAsState()



    var textFieldN by remember { mutableStateOf(TextFieldValue()) }
    var colorFont by remember { mutableStateOf(Color.Black) }
    val maxChars = 2
    CoroutineScope(Dispatchers.Default).launch {
        Calculation.setValueN(4)
    }
    textFieldN=TextFieldValue("4")
    val verticalState = rememberScrollState()
    MaterialTheme {
        Box {
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .alpha(0.6f),
                style = ScrollbarStyle(
                    unhoverColor = Color.LightGray,
                    hoverColor = Color.Black,
                    minimalHeight = 50.dp,
                    hoverDurationMillis = 100,
                    shape = RectangleShape,
                    thickness = 8.dp
                ),

                adapter = rememberScrollbarAdapter(verticalState)
            )
            Column(
                modifier = Modifier
                    .padding(25.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(state = verticalState),
            ) {
                Text(text = "а) Массив вероятностей появления совокупности сообщений на входе дискретного канала P(Y):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    ArrayBlock(arrayA)
                }
                Text(text = "    Энтропия на входе информационного устройства H(Y): $entropy бит")
                Text(text = "б) Массив длительностей сигнала для каждого символа сообщения (ti = 1-N мс):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    ArrayBlock(arrayB)
                }
                Text(text = "    Средняя длительность элементарных сигналов T: $averageDurationOfElementarySignals с\n" +
                            "    Скорость передачи элементарных символов сигнала U: $transmissionRateOfElementarySignalSymbols бит/с")
                Text(text = "в) Матрица вероятностей перехода со входа на выход в канале передачи информации с помехами с учетом технического задания (q=1/(2*N)) P(Y/Z):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    MatrixBlock(matrixC, headerSymbolX = "y", headerSymbolY = "z")
                }



                Text(text = "    Массив вероятностей появления совокупности дискретных сообщений на выходе информационного устройства P(Z):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    ArrayBlock(arrayC)
                }
                Text(text = "    Матрица вероятностей совместных событий P(Y,Z):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    MatrixBlock(matrixC2)
                }
                Text(text = "    Условная энтропия выходного сообщения относительно входного H(Y/Z): $conditionalEntropy бит")
                Text(text = "г) При использовании канала без помех:\n" +
                        "    Пропускная способность C: $bandwidthWithoutInterference бит/с\n" +
                        "    Скорость передачи I(Y): $transferRateWithoutInterference бит/с")
                Text(text = "д) При использовании канала с помехами:\n" +
                        "    Пропускная способность C: $bandwidthWithInterference бит/с\n" +
                        "    Скорость передачи I(Z,Y): $transferRateWithInterference бит/с")
                Row(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .align(Alignment.CenterHorizontally)
                ) {

                    TextField(
                        singleLine = true,
                        value = textFieldN,
                        onValueChange = {
                            if (it.text.length <= maxChars) {
                                textFieldN = it
                                val N = it.text.toIntOrNull()
                                if (N != null) {
                                    if (N > 1) {
                                        colorFont = Color.Black
                                        CoroutineScope(Dispatchers.Default).launch {
                                            Calculation.setValueN(N)
                                        }
                                    } else {
                                        colorFont = Color.Red
                                    }
                                } else {
                                    colorFont = Color.Red
                                }
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = colorFont
                        ),
                        placeholder = {
                            Text(
                                text = "Введите кол-во различных символов",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400,
                                color = Color.LightGray
                            )
                        },
                        modifier = Modifier
                            .width(360.dp)
                            .height(60.dp)
                    )
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Default).launch {
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


            }

        }
    }
}

fun main() = application {
//    val state = rememberWindowState(size = DpSize(700.dp,250.dp))
    Window(onCloseRequest = ::exitApplication, title = "Задание 3", resizable = true) {
        App()
    }
}

@Composable
fun ScrollableBox(modifier: Modifier = Modifier, content: @Composable() (() -> Unit)) {
    Box(modifier = modifier) {
        val verticalState = rememberScrollState()
        val horizontalState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(state = horizontalState)
                .verticalScroll(state = verticalState),
            contentAlignment = Alignment.Center
        ) {
            content.invoke()
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight()
                .alpha(0.6f),
            style = ScrollbarStyle(
                unhoverColor = Color.LightGray,
                hoverColor = Color.Black,
                minimalHeight = 50.dp,
                hoverDurationMillis = 100,
                shape = RectangleShape,
                thickness = 8.dp
            ),

            adapter = rememberScrollbarAdapter(verticalState)
        )
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = 12.dp)
                .alpha(0.6f),
            style = ScrollbarStyle(
                unhoverColor = Color.LightGray,
                hoverColor = Color.Black,
                minimalHeight = 50.dp,
                hoverDurationMillis = 100,
                shape = RectangleShape,
                thickness = 8.dp
            ),
            adapter = rememberScrollbarAdapter(horizontalState)
        )

    }
}

@Composable
fun <T>ArrayBlock(array: Array<T>, headerSymbol: String = "") {
    Row(
        modifier = Modifier
            .background(Color.LightGray),
    ) {
        for (i in -1 until array.size) {
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .height(25.dp)
                    .background(Color.White)
                    .padding(
                        PaddingValues(
                            start = if (i == -1) 0.dp else 2.dp
                        )
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                if (i == -1) {
                    Text(
                        text = headerSymbol, fontSize = 8.sp, overflow = TextOverflow.Ellipsis,
                        maxLines = 1, textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = array[i].toString(), fontSize = 8.sp, overflow = TextOverflow.Ellipsis,
                        maxLines = 1, textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixBlock(matrix: Array<Array<Double>>, headerSymbolX: String = "x", headerSymbolY: String = "y") {
    Row(
        modifier = Modifier
            .background(Color.LightGray),
    ) {
        for (i in -1 until matrix.size) {
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .fillMaxHeight()
            ) {
                for (j in -1 until matrix.size) {
                    Row(
                        modifier = Modifier
                            .height(25.dp)
                            .padding(
                                PaddingValues(
                                    start = if (i == -1) 0.dp else 2.dp,
                                    top = if (j == -1) 0.dp else 2.dp
                                )
                            )
                            .fillMaxWidth()
                            .background(Color.White),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (i == -1) {
                            if (j != -1) {
                                Text(
                                    text = "$headerSymbolY${j+1}", fontSize = 8.sp, overflow = TextOverflow.Ellipsis,
                                    maxLines = 1, textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        } else if (j == -1) {
                            Text(
                                text = "$headerSymbolX${i+1}", fontSize = 8.sp, overflow = TextOverflow.Ellipsis,
                                maxLines = 1, textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                            )
                        } else {
                            Text(
                                text = matrix[j][i].toString(), fontSize = 8.sp, overflow = TextOverflow.Ellipsis,
                                maxLines = 1, textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }

        }
    }
}