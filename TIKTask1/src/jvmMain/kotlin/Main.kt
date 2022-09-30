// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val valueH by Calculation.valueH.collectAsState()
    val valueI by Calculation.valueI.collectAsState()
    val arrayA by Calculation.arrayA.collectAsState()

    var textFieldN by remember { mutableStateOf(TextFieldValue()) }
    var colorFont by remember { mutableStateOf(Color.Black) }

    val maxChars = 3
    textFieldN=TextFieldValue("32")
    CoroutineScope(Dispatchers.Default).launch {
        Calculation.setValueN(32)
    }
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
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "a) Массив вероятностей появления совокупности дискретных сообщений на входе информационного устройства P(X):")
                ScrollableBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    ArrayBlock(arrayA)
                }
                Text(text = "б) Среднее количество информации в совокупности сообщений: \n$valueI")
                Text(text = "в) Максимальная энтропия сгенерированной совокупности: \n$valueH")
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
                                text = "Введите кол-во сообщений",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400,
                                color = Color.LightGray
                            )
                        },
                        modifier = Modifier
                            .width(300.dp)
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
//    val state = rememberWindowState(size = DpSize(500.dp,250.dp))
    Window(onCloseRequest = ::exitApplication, title = "Задание 1", resizable = true) {
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
fun ArrayBlock(array: Array<Double>, headerSymbol: String = "") {
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
