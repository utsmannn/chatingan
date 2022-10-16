package com.utsman.chatingan.common

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import kotlin.reflect.KClass

fun Context.toast(message: String?) =
    Toast.makeText(this, message.orEmpty(), Toast.LENGTH_SHORT).show()

fun nothing(): Nothing = Nothing::class.java.newInstance()


fun <T: ComponentActivity>Context.launch(clazz: KClass<T>) {
    startActivity(
        Intent(this, clazz.java)
    )
}