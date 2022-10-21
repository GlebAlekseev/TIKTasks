package compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun <T>MatrixBlock(matrix: Array<Array<T>>, headerSymbolX: String = "x", headerSymbolY: String = "y", isHeader: Boolean = true) {
    if (matrix.isEmpty()) return
    val start = if (isHeader) -1 else 0
    Row(
        modifier = Modifier
            .background(Color.LightGray),
    ) {
        for (i in start until matrix[0].size) {
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .fillMaxHeight()
            ) {
                for (j in start until matrix.size) {
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