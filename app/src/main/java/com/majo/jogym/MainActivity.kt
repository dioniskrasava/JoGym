package com.majo.jogym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.majo.jogym.ui.JoGymApp
import com.majo.jogym.ui.theme.JoGymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JoGymTheme {
                JoGymApp()
            }
        }
    }
}