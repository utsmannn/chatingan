package com.utsman.chatingan.main.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import coil.compose.AsyncImage
import com.utsman.chatingan.common.ui.component.ColumnCenter

@Composable
fun ShowQr(bitmap: Bitmap) {
    ColumnCenter {
        AsyncImage(model = bitmap, contentDescription = "")
    }
}