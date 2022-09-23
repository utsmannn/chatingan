package com.utsman.chatingan.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.utsman.chatingan.common.ui.component.ColumnCenter


@Composable
fun LoadingScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    ColumnCenter(modifier = modifier) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyScreen() {
    ColumnCenter {
        Text(text = "Empty")
    }
}

@Composable
fun IdleScreen(content: @Composable () -> Unit) {
    content.invoke()
}

@Composable
fun FailureScreen(message: String) {
    ColumnCenter {
        Text(text = message, color = Color.Red)
    }
}

@Composable
fun Modifier.clickableRipple(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
        indication = rememberRipple(bounded = true)
    )
}