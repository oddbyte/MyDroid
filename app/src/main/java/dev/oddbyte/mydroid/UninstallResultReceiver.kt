package dev.oddbyte.mydroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast

class UninstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Uninstallation succeeded", Toast.LENGTH_LONG).show()
                // Optionally, refresh the app list or navigate back
                val installedAppsIntent = InstalledAppsActivity.newIntent(context)
                installedAppsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(installedAppsIntent)
            }
            else -> {
                if (message == null) {
                    Toast.makeText(context, "Uninstallation succeeded", Toast.LENGTH_LONG).show()
                } else Toast.makeText(context, "Uninstallation failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }
}
