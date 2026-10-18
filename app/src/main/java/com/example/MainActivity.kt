package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.GymAppRoot
import com.example.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mandatory edge-to-edge full bleed display
        enableEdgeToEdge()
        setContent {
            GymTrackerTheme {
                GymAppRoot()
            }
        }
    }
}
