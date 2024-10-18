# MyDroid
## It's your phone, you should have control over it
### Gives you access to "hidden" settings that your device manufacturer might hide from you.

## IMPORTANT:
You *MUST* remove **ALL** accounts (Google accounts, etc.) on your device **before setup**.

<sup>You can add them back after setup</sup>

If you get error message `Not allowed to set the profile/device owner because there are already some accounts on the device` make sure all accounts are removed.

You can use the ADB command `dumpsys account | grep -A 3 Accounts:` to reveal remaining accounts (including the hidden ones on some devices).

## Installation:
- Download and install the APK from the Releases tab
- Open ADB shell and run this command to grant permissions:
```
dpm set-device-owner 'dev.oddbyte.mydroid/.MyDeviceAdminReceiver' && pm grant dev.oddbyte.mydroid android.permission.REQUEST_INSTALL_PACKAGES && pm grant dev.oddbyte.mydroid android.permission.PACKAGE_USAGE_STATS && pm grant dev.oddbyte.mydroid android.permission.WRITE_SECURE_SETTINGS && pm grant dev.oddbyte.mydroid android.permission.REBOOT && pm grant dev.oddbyte.mydroid android.permission.SET_TIME && pm grant dev.oddbyte.mydroid android.permission.SET_TIME_ZONE && pm grant dev.oddbyte.mydroid android.permission.MANAGE_USERS && pm grant dev.oddbyte.mydroid android.permission.ACCESS_NOTIFICATION_POLICY && pm grant dev.oddbyte.mydroid android.permission.DISABLE_KEYGUARD && pm grant dev.oddbyte.mydroid android.permission.READ_PHONE_STATE
```

## Pictures:

![image](https://github.com/user-attachments/assets/c0b5906a-8d70-49bc-ac76-822f6eca373e) ![image](https://github.com/user-attachments/assets/5db49a80-5f41-4d00-baca-50549004f620) ![image](https://github.com/user-attachments/assets/c04ea1a7-983d-433a-b813-f1e5cd27516e) ![image](https://github.com/user-attachments/assets/4bb24c01-da25-4e76-b5a5-df4f811e1583)

