package com.ubeyd.focusguard

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(
    onOpenAccessibilitySettings: () -> Unit
) {
    val context = LocalContext.current

    val preferencesManager =
        remember {
            PreferencesManager(context.applicationContext)
        }

    val blockInstagramReels by
        preferencesManager.blockInstagramReels
            .collectAsStateWithLifecycle(
                initialValue = true
            )

    val blockYouTubeShorts by
        preferencesManager.blockYouTubeShorts
            .collectAsStateWithLifecycle(
                initialValue = true
            )

    var accessibilityEnabled by
        remember {
            mutableStateOf(
                isAccessibilityServiceEnabled(context)
            )
        }

    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled =
                isAccessibilityServiceEnabled(context)

            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        item {

            Text(
                text = "FocusGuard",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Reels ve Shorts kullanımını otomatik olarak engelle.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Erişilebilirlik servisi",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            if (accessibilityEnabled) {
                                "Aktif"
                            } else {
                                "Devre dışı"
                            },
                        color =
                            if (accessibilityEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick =
                            onOpenAccessibilitySettings,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                if (accessibilityEnabled) {
                                    "Ayarları Aç"
                                } else {
                                    "Erişilebilirliği Etkinleştir"
                                }
                        )
                    }
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column {

                    Text(
                        text = "Engelleme",
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp
                            ),
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    SettingSwitch(
                        title = "Instagram Reels",
                        enabled = blockInstagramReels,
                        onCheckedChange = { enabled ->

                            kotlinx.coroutines.MainScope()
                                .launch {
                                    preferencesManager
                                        .setBlockInstagramReels(
                                            enabled
                                        )
                                }
                        }
                    )

                    HorizontalDivider()

                    SettingSwitch(
                        title = "YouTube Shorts",
                        enabled = blockYouTubeShorts,
                        onCheckedChange = { enabled ->

                            kotlinx.coroutines.MainScope()
                                .launch {
                                    preferencesManager
                                        .setBlockYouTubeShorts(
                                            enabled
                                        )
                                }
                        }
                    )
                }
            }
        }

        item {

            Text(
                text =
                    "FocusGuard yalnızca Instagram ve YouTube üzerinde çalışır.",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            style =
                MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun isAccessibilityServiceEnabled(
    context: Context
): Boolean {

    val accessibilityManager =
        context.getSystemService(
            Context.ACCESSIBILITY_SERVICE
        ) as? AccessibilityManager
            ?: return false

    val enabledServices =
        accessibilityManager
            .getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )

    for (service in enabledServices) {

        val serviceInfo =
            service.resolveInfo
                ?.serviceInfo
                ?: continue

        if (
            serviceInfo.packageName ==
                context.packageName &&
            serviceInfo.name ==
                ReelsBlockerService::class.java.name
        ) {
            return true
        }
    }

    return false
}
