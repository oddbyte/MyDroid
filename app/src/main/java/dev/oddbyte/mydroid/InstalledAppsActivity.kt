package dev.oddbyte.mydroid

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.oddbyte.mydroid.ui.theme.MyDroidTheme

class InstalledAppsActivity : ComponentActivity() {
    private var showSystemApps by mutableStateOf(false)
    private var showFrameworkApps by mutableStateOf(false)
    private var installedApps by mutableStateOf(emptyList<ApplicationInfo>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshAppsList()

        setContent {
            MyDroidTheme {
                InstalledAppsScreen(
                    apps = installedApps,
                    packageManager = this.packageManager,
                    onAppClick = { appInfo ->
                        openAppDetails(appInfo.packageName)
                    },
                    onToggleSystemApps = { toggleSystemApps() },
                    onToggleFrameworkApps = { toggleFrameworkApps() },
                    showSystemApps = showSystemApps,
                    showFrameworkApps = showFrameworkApps,
                    onRefreshClick = { refreshAppsList() },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun getInstalledApps(showSystem: Boolean, showFramework: Boolean): List<ApplicationInfo> {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return apps.filter { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM != 0)
            val isFrameworkApp = app.packageName.startsWith("android") || app.packageName.startsWith("com.android")
            val isUserInstalled = (app.flags and ApplicationInfo.FLAG_SYSTEM == 0)

            Log.d("InstalledApps", "App: ${app.packageName}, System: $isSystemApp, Framework: $isFrameworkApp, UserInstalled: $isUserInstalled")

            when {
                showSystem && showFramework -> true // Show all apps
                showSystem -> !isFrameworkApp // Show system but not framework apps
                showFramework -> !isSystemApp // Show framework but not system apps
                else -> isUserInstalled // Only show user-installed apps by default
            }
        }
    }

    private fun toggleSystemApps() {
        showSystemApps = !showSystemApps
        refreshAppsList()
    }

    private fun toggleFrameworkApps() {
        showFrameworkApps = !showFrameworkApps
        refreshAppsList()
    }

    private fun refreshAppsList() {
        installedApps = getInstalledApps(showSystemApps, showFrameworkApps)
    }

    private fun openAppDetails(packageName: String) {
        val intent = Intent(this, AppDetailsActivity::class.java).apply {
            putExtra("packageName", packageName)
        }
        startActivity(intent)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, InstalledAppsActivity::class.java)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppsScreen(
    apps: List<ApplicationInfo>,
    packageManager: PackageManager,
    onAppClick: (ApplicationInfo) -> Unit,
    onToggleSystemApps: () -> Unit,
    onToggleFrameworkApps: () -> Unit,
    showSystemApps: Boolean,
    showFrameworkApps: Boolean,
    onRefreshClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installed Apps") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Show System Apps") },
                            onClick = { onToggleSystemApps() },
                            trailingIcon = {
                                Checkbox(
                                    checked = showSystemApps,
                                    onCheckedChange = { onToggleSystemApps() }
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Show Framework Apps") },
                            onClick = { onToggleFrameworkApps() },
                            trailingIcon = {
                                Checkbox(
                                    checked = showFrameworkApps,
                                    onCheckedChange = { onToggleFrameworkApps() }
                                )
                            }
                        )
                    }
                    // Refresh button
                    IconButton(onClick = onRefreshClick) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Apps") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(apps.filter {
                        it.loadLabel(packageManager).toString().contains(searchQuery.text, ignoreCase = true)
                    }) { app ->
                        AppRow(
                            appName = app.loadLabel(packageManager).toString(),
                            packageName = app.packageName,
                            appIcon = app.loadIcon(packageManager),
                            onClick = { onAppClick(app) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AppRow(appName: String, packageName: String, appIcon: Any, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(appIcon),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = appName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
