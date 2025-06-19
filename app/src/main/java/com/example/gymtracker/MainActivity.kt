package com.example.gymtracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymTrackerTheme {
                GymTrackerApp()
            }
        }
    }
}

@Composable
fun GymTrackerApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("helloworld")
        }
    }
}

@Preview(showBackground = true, locale = "fi", showSystemUi = true)
@Composable
private fun ThemePreview() {
    GymTrackerTheme {
        ThemePreviewColumn()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun ThemePreviewDark() {
    GymTrackerTheme(
        darkTheme = true
    ) {
        ThemePreviewColumn()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemePreviewColumn() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("This is headline", style = MaterialTheme.typography.headlineSmall)
            Text("This is body", style = MaterialTheme.typography.bodyLarge)
            Text("This is label", style = MaterialTheme.typography.labelMedium)

            FlowRow {
                Button(onClick = {}) {
                    Text("Button")
                }
                ElevatedButton(onClick = {}) {
                    Text("Elevated")
                }
                OutlinedButton(onClick = {}) {
                    Text("Outlined")
                }
            }

            TextField(value = "Text field", onValueChange = { }, label = { Text("Text Field") })
            OutlinedTextField(
                value = "Outlined text field",
                onValueChange = { },
                label = { Text("Outlined Field") }
            )

            FlowRow {
                Card {
                    Text("Card Content", modifier = Modifier.padding(8.dp))
                }
                OutlinedCard {
                    Text("Outlined card", modifier = Modifier.padding(8.dp))
                }
                ElevatedCard {
                    Text("Elevated Card", modifier = Modifier.padding(8.dp))
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "⚠️ Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "This is an error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            FlowRow {
                Surface {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 50.dp,
                    shadowElevation = 2.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 100.dp,
                    shadowElevation = 4.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 500.dp,
                    shadowElevation = 6.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 1000.dp,
                    shadowElevation = 8.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    checked = true,
                    onCheckedChange = {},
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text("Checked Surface", modifier = Modifier.padding(8.dp))
                }
            }

            FlowRow {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Checkbox(checked = false, onCheckedChange = {})
                Checkbox(checked = true, onCheckedChange = {})
                Switch(checked = false, onCheckedChange = {})
                Switch(checked = true, onCheckedChange = {})
                RadioButton(selected = false, onClick = {})
                RadioButton(selected = true, onClick = {})
            }

            Slider(value = 0.5f, onValueChange = {})

            FlowRow {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "FAB")
                }
                LinearProgressIndicator(
                    progress = {0.5f},
                )
                CircularProgressIndicator()
            }

            HorizontalDivider()

        }
    }
}
