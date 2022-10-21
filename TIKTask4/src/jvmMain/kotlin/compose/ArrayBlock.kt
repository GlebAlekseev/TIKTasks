package compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
