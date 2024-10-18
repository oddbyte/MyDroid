package dev.oddbyte.mydroid

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.UserManager
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.oddbyte.mydroid.ui.theme.MyDroidTheme

@OptIn(ExperimentalMaterial3Api::class)
class RestrictionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyDroidTheme {
                RestrictionsScreen(onBackClick = { finish() })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RestrictionsScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        // List of restrictions (removing deprecated ones)
        val restrictionList = listOf(
            "Disallow Add User" to UserManager.DISALLOW_ADD_USER,
            "Disallow Add Wifi Config" to UserManager.DISALLOW_ADD_WIFI_CONFIG,
            "Disallow Adjust Volume" to UserManager.DISALLOW_ADJUST_VOLUME,
            "Disallow Airplane Mode" to UserManager.DISALLOW_AIRPLANE_MODE,
            "Disallow Apps Control" to UserManager.DISALLOW_APPS_CONTROL,
            "Disallow Autofill" to UserManager.DISALLOW_AUTOFILL,
            "Disallow Bluetooth" to UserManager.DISALLOW_BLUETOOTH,
            "Disallow Bluetooth Sharing" to UserManager.DISALLOW_BLUETOOTH_SHARING,
            "Disallow Camera" to UserManager.DISALLOW_CAMERA_TOGGLE,
            "Disallow Microphone" to UserManager.DISALLOW_MICROPHONE_TOGGLE,
            "Disallow Unmute Microphone" to UserManager.DISALLOW_UNMUTE_MICROPHONE,
            "Disallow Change Wifi State" to UserManager.DISALLOW_CHANGE_WIFI_STATE,
            "Disallow Config Bluetooth" to UserManager.DISALLOW_CONFIG_BLUETOOTH,
            "Disallow Config Brightness" to UserManager.DISALLOW_CONFIG_BRIGHTNESS,
            "Disallow Config Credentials" to UserManager.DISALLOW_CONFIG_CREDENTIALS,
            "Disallow Config Date Time" to UserManager.DISALLOW_CONFIG_DATE_TIME,
            "Disallow Config Default Apps" to UserManager.DISALLOW_CONFIG_DEFAULT_APPS,
            "Disallow Config Locale" to UserManager.DISALLOW_CONFIG_LOCALE,
            "Disallow Config Location" to UserManager.DISALLOW_CONFIG_LOCATION,
            "Disallow Config Mobile Networks" to UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,
            "Disallow Config Screen Timeout" to UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,
            "Disallow Config Tethering" to UserManager.DISALLOW_CONFIG_TETHERING,
            "Disallow Config VPN" to UserManager.DISALLOW_CONFIG_VPN,
            "Disallow Config Wifi" to UserManager.DISALLOW_CONFIG_WIFI,
            "Disallow Create Windows" to UserManager.DISALLOW_CREATE_WINDOWS,
            "Disallow Data Roaming" to UserManager.DISALLOW_DATA_ROAMING,
            "Disallow Debugging Features" to UserManager.DISALLOW_DEBUGGING_FEATURES,
            "Disallow Factory Reset" to UserManager.DISALLOW_FACTORY_RESET,
            "Disallow Fun" to UserManager.DISALLOW_FUN,
            "Disallow Grant Admin" to UserManager.DISALLOW_GRANT_ADMIN,
            "Disallow Install Apps" to UserManager.DISALLOW_INSTALL_APPS,
            "Disallow Install Unknown Sources" to UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            "Disallow Modify Accounts" to UserManager.DISALLOW_MODIFY_ACCOUNTS,
            "Disallow Mount Physical Media" to UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            "Disallow Network Reset" to UserManager.DISALLOW_NETWORK_RESET,
            "Disallow Outgoing Calls" to UserManager.DISALLOW_OUTGOING_CALLS,
            "Disallow Safe Boot" to UserManager.DISALLOW_SAFE_BOOT,
            "Disallow Set User Icon" to UserManager.DISALLOW_SET_USER_ICON,
            "Disallow Set Wallpaper" to UserManager.DISALLOW_SET_WALLPAPER,
            "Disallow Share Into Managed Profile" to UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,
            "Disallow Share Location" to UserManager.DISALLOW_SHARE_LOCATION,
            "Disallow SMS" to UserManager.DISALLOW_SMS,
            "Disallow Uninstall Apps" to UserManager.DISALLOW_UNINSTALL_APPS,
            "Disallow USB File Transfer" to UserManager.DISALLOW_USB_FILE_TRANSFER,
            "Disallow User Switch" to UserManager.DISALLOW_USER_SWITCH,
            "Disallow Wifi Direct" to UserManager.DISALLOW_WIFI_DIRECT,
            "Disallow Wifi Tethering" to UserManager.DISALLOW_WIFI_TETHERING,
        ).sortedBy { it.first }

        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var filteredRestrictions by remember { mutableStateOf(restrictionList) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("User Restrictions") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Restrictions") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            filteredRestrictions = restrictionList.filter {
                                it.first.contains(searchQuery.text, ignoreCase = true)
                            }
                        }
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(filteredRestrictions.size) { index ->
                        val (label, restrictionKey) = filteredRestrictions[index]
                        var isChecked by remember {
                            // Use getUserRestrictions for API 31 onwards safely
                            val userRestrictions = devicePolicyManager.getUserRestrictions(adminComponent)
                            mutableStateOf(userRestrictions.getBoolean(restrictionKey, false))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                                Text(text = restrictionKey, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }

                            Switch(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                                        if (it) {
                                            devicePolicyManager.addUserRestriction(adminComponent, restrictionKey)
                                        } else {
                                            devicePolicyManager.clearUserRestriction(adminComponent, restrictionKey)
                                        }
                                        Toast.makeText(
                                            context,
                                            "$label restriction updated to $it",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
