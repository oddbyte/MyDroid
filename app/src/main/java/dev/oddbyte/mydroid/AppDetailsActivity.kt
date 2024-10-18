package dev.oddbyte.mydroid

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.oddbyte.mydroid.ui.theme.MyDroidTheme

class AppDetailsActivity : ComponentActivity() {
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        val packageName = intent.getStringExtra("packageName") ?: return
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

        // Retrieve the uninstall block status
        val isUninstallBlocked = devicePolicyManager.isUninstallBlocked(adminComponent, packageName)

        setContent {
            MyDroidTheme {
                AppDetailsScreen(
                    appInfo = appInfo,
                    packageManager = this.packageManager,
                    initialBlockUninstall = isUninstallBlocked, // Pass the status here
                    onBackClick = {
                        val installedAppsIntent = InstalledAppsActivity.newIntent(this)
                        this.startActivity(installedAppsIntent)
                        finish()
                    },
                    onUninstallClick = { uninstallApp(this, packageName) },
                    onBlockUninstallClick = {
                        toggleUninstallBlock(packageName)
                    },
                    onManagePermissionsClick = { openAppPermissions(packageName) }
                )
            }
        }
    }

    private fun uninstallApp(context: Context, packageName: String) {
        try {
            val packageInstaller: PackageInstaller = context.packageManager.packageInstaller

            // Create an IntentSender to receive the uninstallation result
            val intent = Intent(context, UninstallResultReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val statusReceiver = pendingIntent.intentSender

            // Remove uninstall blocks, if there are any
            devicePolicyManager.setUninstallBlocked(adminComponent, packageName, false)

            // Start the uninstallation
            packageInstaller.uninstall(packageName, statusReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleUninstallBlock(packageName: String) {
        val currentStatus = devicePolicyManager.isUninstallBlocked(adminComponent, packageName)
        Log.d("Toggle Uninstall Block", ("Toggling uninstall block to: " + !currentStatus))
        devicePolicyManager.setUninstallBlocked(adminComponent, packageName, !currentStatus)
    }

    private fun openAppPermissions(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    appInfo: ApplicationInfo,
    packageManager: PackageManager,
    initialBlockUninstall: Boolean, // Accept the initial value
    onBackClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onBlockUninstallClick: () -> Unit,
    onManagePermissionsClick: () -> Unit
) {
    var blockUninstall by remember { mutableStateOf(initialBlockUninstall) } // Initialize with the actual status

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appInfo.loadLabel(packageManager).toString()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Package: ${appInfo.packageName}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onManagePermissionsClick, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Permissions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Block Uninstall Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Block Uninstall")
                Checkbox(
                    checked = blockUninstall,
                    onCheckedChange = {
                        blockUninstall = it
                        onBlockUninstallClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onUninstallClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Uninstall App", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}