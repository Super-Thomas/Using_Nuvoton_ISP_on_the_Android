# Using_Nuvoton_ISP_on_the_Android

## Intro
You can ISP firmware update to Nuvoton MCU on the Android with this project. I will use USB HID in Android Device for this. USB HID protocol is follow to “NuMicro ISP Programming Tool version 4.03 for Windows OS” provided by Nuvoton. So if you programed ISP firmware for USB HID in Nuvoton SDK to your Nuvoton parts, your Nuvoton parts can connect to Android app in this project with USB HID.<br />
If you use Bridge firmware in this project, you can ISP firmware update to MCU not support USB. In fact, Bridge firmware is not required if you will use USB CDC in Android. But if you want to implement another function in Bridge firmware, it would be better choice.

## Development environment
### For Android app
|N|Name|Description|Note|
|---|---|---|---|
|1|Android OS|Version 6.0 - Marshmallow (API level 23)||
|2|Android Studio|Version Bumblebee 2021.1.1 Patch2|IDE|
