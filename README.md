# MyDroid
## It's your phone, you should have control over it
### Gives you access to "hidden" permissions that your device manufacturer might hide from you.

## Installation:
- Download and install the APK from the Releases tab
- Open ADB shell and run this command to grant permissions:
```
dpm set-device-owner 'dev.oddbyte.mydroid/.MyDeviceAdminReceiver' && pm grant dev.oddbyte.mydroid android.permission.REQUEST_INSTALL_PACKAGES && pm grant dev.oddbyte.mydroid android.permission.PACKAGE_USAGE_STATS && pm grant dev.oddbyte.mydroid android.permission.WRITE_SECURE_SETTINGS && pm grant dev.oddbyte.mydroid android.permission.WRITE_SETTINGS && pm grant dev.oddbyte.mydroid android.permission.REBOOT && pm grant dev.oddbyte.mydroid android.permission.SET_TIME && pm grant dev.oddbyte.mydroid android.permission.SET_TIME_ZONE && pm grant dev.oddbyte.mydroid android.permission.MANAGE_USERS && pm grant dev.oddbyte.mydroid android.permission.ACCESS_NOTIFICATION_POLICY && pm grant dev.oddbyte.mydroid android.permission.DISABLE_KEYGUARD && pm grant dev.oddbyte.mydroid android.permission.READ_PHONE_STATE
```
## IMPORTANT:
You *MUST* remove **ALL** accounts (Google accounts, etc.) on your device.

If you get error message `Not allowed to set the profile/device owner because there are already some accounts on the device` make sure all accounts are removed.

You can use the ADB command `dumpsys account | grep -A 3 Accounts:` to reveal remaining accounts (including the hidden ones on some devices).
