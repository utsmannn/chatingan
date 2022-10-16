package com.utsman.chatingan.common.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ButtonColorsDefault(): ButtonColors {
    return ButtonDefaults.buttonColors(
        backgroundColor = Color.Blue
    )
}

@Composable
fun ButtonColorsWhite(): ButtonColors {
    return ButtonDefaults.buttonColors(
        backgroundColor = Color.White
    )
}

@Composable
fun ButtonImage(
    resources: Int,
    text: String,
    buttonColors: ButtonColors = ButtonColorsDefault(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = buttonColors
    ) {
        Image(
            painter = painterResource(resources),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(end = 6.dp)
        )
        ChatinganText(text)
    }
}

@Composable
fun ButtonImage(
    resources: Int,
    buttonColors: ButtonColors = ButtonColorsDefault(),
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit = {}
) {
    Button(
        onClick = onClick,
        colors = buttonColors
    ) {
        Image(
            painter = painterResource(resources),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        content.invoke(this)
    }
}