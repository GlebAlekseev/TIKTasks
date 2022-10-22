package compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp


@Composable
fun ScrollableBox(modifier: Modifier = Modifier, content: @Composable() (() -> Unit)) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val verticalState = rememberScrollState()
        val horizontalState = rememberScrollState()
        Box(
            modifier = Modifier
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