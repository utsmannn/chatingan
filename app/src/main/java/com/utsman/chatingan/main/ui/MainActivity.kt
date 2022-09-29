package com.utsman.chatingan.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.firebase.FirebaseApp
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.auth.component.authComponentBuilder

class MainActivity : ComponentActivity() {
    private val authComponent: AuthComponent by authComponentBuilder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatinganApp(authComponent = authComponent)
        }

        // root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
        //     public void onGlobalLayout(){
        //           int heightDiff = root.getRootView().getHeight()- root.getHeight();
        //           // IF height diff is more then 150, consider keyboard as visible.
        //        }
        //  });

        /*window.decorView.rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff =
        }*/


    }
}