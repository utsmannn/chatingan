package com.utsman.chatingan.common.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun Loading() {
    ColumnCenter {
        Text(text = "loading...")
    }
}

@Composable
fun Empty() {
    ColumnCenter {
    }
}

@Composable
fun Idle(content: @Composable () -> Unit) {
    content.invoke()
}

@Composable
fun Failure(message: String) {
    ColumnCenter {
        Text(text = message, color = Color.Red)
    }
}