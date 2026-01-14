package com.nagutos.nyaaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nagutos.nyaaandroid.ui.HomeScreen
import com.nagutos.nyaaandroid.ui.theme.NyaaAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NyaaAndroidTheme {
                    HomeScreen();
            }
        }
    }
}