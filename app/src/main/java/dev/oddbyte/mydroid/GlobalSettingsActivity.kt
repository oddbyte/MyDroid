package dev.oddbyte.mydroid

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.oddbyte.mydroid.ui.theme.MyDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
class GlobalSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyDroidTheme {
                GlobalSettingsScreen(onBackClick = { finish() })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GlobalSettingsScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)
        var searchQuery by remember { mutableStateOf("") }

        val boolSettings: List<Pair<String, String>> = listOf(
            "ADB Enabled" to Settings.Global.ADB_ENABLED,
            "Auto Time" to Settings.Global.AUTO_TIME,
            "Auto Time Zone" to Settings.Global.AUTO_TIME_ZONE,
            "Data Roaming" to Settings.Global.DATA_ROAMING,
            "Stay On While Plugged In" to Settings.Global.STAY_ON_WHILE_PLUGGED_IN
        )

        val numericSettings: List<Pair<String, String>> = listOf(
        )

        val textSettings: List<Pair<String, String>> = listOf(
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Global Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Filter settings based on search query
                    val filteredBoolSettings = boolSettings.filter { it.first.contains(searchQuery, ignoreCase = true) }
                    val filteredNumericSettings = numericSettings.filter { it.first.contains(searchQuery, ignoreCase = true) }
                    val filteredTextSettings = textSettings.filter { it.first.contains(searchQuery, ignoreCase = true) }

                    items(filteredBoolSettings.size) { index ->
                        val (label, settingKey) = filteredBoolSettings[index]
                        var isChecked by remember {
                            mutableStateOf(
                                Settings.Global.getInt(context.contentResolver, settingKey, 0) == 1
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label)
                            Switch(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                                        val value = if (it) "1" else "0"
                                        devicePolicyManager.setGlobalSetting(adminComponent, settingKey, value)
                                        Toast.makeText(context, "$label updated to $it", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    // Numeric settings with sliders
                    items(filteredNumericSettings.size) { index ->
                        val (label, settingKey) = filteredNumericSettings[index]
                        var sliderValue by remember {
                            mutableStateOf(
                                Settings.Secure.getInt(context.contentResolver, settingKey, 30000).toFloat() // Default 30 seconds
                            )
                        }
                        var textValue by remember { mutableStateOf(sliderValue.toInt().toString()) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = label)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        sliderValue = it
                                        textValue = it.toInt().toString()
                                    },
                                    valueRange = 15000f..600000f,
                                    steps = 58,
                                    onValueChangeFinished = {
                                        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                                            val newValue = sliderValue.toInt().toString()
                                            devicePolicyManager.setSecureSetting(adminComponent, settingKey, newValue)
                                            Toast.makeText(context, "$label updated to ${sliderValue.toInt()} ms", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        val newValue = it.toFloatOrNull() ?: sliderValue
                                        sliderValue = newValue.coerceIn(15000f, 600000f)
                                    },
                                    modifier = Modifier.width(100.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }

                    items(filteredTextSettings.size) { index ->
                        val (label, settingKey) = filteredTextSettings[index]
                        var textValue by remember {
                            mutableStateOf(
                                Settings.Global.getString(context.contentResolver, settingKey) ?: ""
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = label)
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { textValue = it },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                                            devicePolicyManager.setGlobalSetting(adminComponent, settingKey, textValue)
                                            Toast.makeText(context, "$label updated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}