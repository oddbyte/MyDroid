package dev.oddbyte.mydroid

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.oddbyte.mydroid.ui.theme.MyDroidTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {

    enum class PermissionStatus {
        AllGranted,
        MissingDeviceOwnerOrWriteSecureSettings,
        MissingWriteSettings
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyDroidTheme {
                val context = LocalContext.current
                var permissionStatus by remember { mutableStateOf(handlePermissions(context)) }

                // UI will recompose when permissionStatus changes
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("MyDroid", style = MaterialTheme.typography.titleLarge) })
                    },
                    content = { paddingValues ->
                        when (permissionStatus) {
                            PermissionStatus.AllGranted -> {
                                DeviceOwnerContent(paddingValues, context)
                            }
                            PermissionStatus.MissingDeviceOwnerOrWriteSecureSettings -> {
                                MissingPermissionsContent(
                                    message = "Device Owner, WRITE_SECURE_SETTINGS, or CHANGE_WIFI_STATE not granted. Please visit the GitHub for setup instructions:",
                                    showGithubButton = true,
                                    showSettingsButton = false,
                                    onRefresh = { permissionStatus = handlePermissions(context) }
                                )
                            }
                            PermissionStatus.MissingWriteSettings -> {
                                MissingPermissionsContent(
                                    message = "WRITE_SETTINGS permission is required. Please grant it in settings.",
                                    showGithubButton = false,
                                    showSettingsButton = true,
                                    onRefresh = { permissionStatus = handlePermissions(context) }
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handlePermissions(context: Context): PermissionStatus {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isDeviceOwner = devicePolicyManager.isDeviceOwnerApp(context.packageName)
        val missingPermissions = checkAllPermissions(context)
        val canWriteSettings = Settings.System.canWrite(context)

        return when {
            !canWriteSettings -> PermissionStatus.MissingWriteSettings
            !isDeviceOwner || missingPermissions.contains(Manifest.permission.WRITE_SECURE_SETTINGS) -> PermissionStatus.MissingDeviceOwnerOrWriteSecureSettings
            else -> PermissionStatus.AllGranted
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceOwnerContent(paddingValues: PaddingValues, context: Context) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Lock Device", description = "Lock the device instantly.") {
                lockDevice(context)
            }
            ActionCard(icon = Icons.Filled.Refresh, title = "Reboot", description = "Reboot the device.") {
                reboot(context)
            }
            ActionCard(icon = Icons.Filled.Menu, title = "Installed Apps", description = "View all installed apps.") {
                showInstalledApps(context)
            }
            ActionCard(icon = Icons.Filled.KeyboardArrowDown, title = "Install APK", description = "Select and install an APK.") {
                selectAPKAndInstall()
            }
            ActionCard(icon = Icons.Filled.Lock, title = "Restrictions", description = "Manage device and user restrictions.") {
                goToRestrictions(context)
            }
            ActionCard(icon = Icons.Filled.Settings, title = "Global Settings", description = "Go to global settings.") {
                goToGlobalSettings(context)
            }
        }
    }

    @Composable
    fun ActionCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    @Composable
    fun MissingPermissionsContent(
        message: String,
        showGithubButton: Boolean,
        showSettingsButton: Boolean,
        onRefresh: () -> Unit
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (showSettingsButton) {
                Button(onClick = { openWriteSettings(context) }) {
                    Text("Grant WRITE_SETTINGS")
                }
            }
            if (showGithubButton) {
                Button(onClick = { openGithubInstallation(context) }) {
                    Text("Visit GitHub for Setup Instructions")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }

    private fun openWriteSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }

    private fun openGithubInstallation(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://github.com/oddbyte/MyDroid#installation")
        }
        context.startActivity(intent)
    }

    private fun checkAllPermissions(context: Context): List<String> {
        val permissions = listOf(
            Manifest.permission.WRITE_SECURE_SETTINGS
        )

        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    private val selectAPKFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            installAPKSilently(it)
        }
    }

    private fun selectAPKAndInstall() {
        selectAPKFile.launch(arrayOf("application/vnd.android.package-archive"))
    }

    private fun installAPKSilently(apkUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(apkUri)
            inputStream?.let {
                installPackageSilently(this, it)
            } ?: run {
                Toast.makeText(this, "Failed to open APK file", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun installPackageSilently(
        context: Context,
        inputStream: InputStream
    ) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        try {
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            val out = session.openWrite("MyAppInstall", 0, -1)
            val buffer = ByteArray(65536)
            var c: Int

            while (inputStream.read(buffer).also { c = it } != -1) {
                out.write(buffer, 0, c)
            }

            session.fsync(out)
            inputStream.close()
            out.close()

            val intent = Intent(context, InstallResultReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val statusReceiver = pendingIntent.intentSender

            session.commit(statusReceiver)
        } catch (e: Exception) {
            Toast.makeText(context, "Installation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun lockDevice(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            try {
                devicePolicyManager.lockNow()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to lock device: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reboot(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            try {
                devicePolicyManager.reboot(adminComponent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to reboot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Not a device owner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToGlobalSettings(context: Context) {
        val intent = Intent(context, GlobalSettingsActivity::class.java)
        context.startActivity(intent)
    }

    private fun showInstalledApps(context: Context) {
        val intent = InstalledAppsActivity.newIntent(context)
        context.startActivity(intent)
    }

    private fun goToRestrictions(context: Context) {
        val intent = Intent(context, RestrictionsActivity::class.java)
        context.startActivity(intent)
    }
}
