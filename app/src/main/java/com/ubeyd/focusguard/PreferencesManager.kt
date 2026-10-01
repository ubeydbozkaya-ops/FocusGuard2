package com.ubeyd.focusguard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            MaterialTheme {

                Surface {

                    MainScreen(
                        onOpenAccessibilitySettings = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
