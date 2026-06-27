package com.rnx.laranjada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rnx.laranjada.core.design.theme.LaranjadaTheme
import com.rnx.laranjada.feature.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            LaranjadaTheme {
                HomeScreen()
            }
        }
    }
}
