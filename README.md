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

### For Bridge firmware
|N|Name|Description|Note|
|---|---|---|---|
|1|ARM MDK|Keil uVision Version 5.34.0.0|IDE|
|2|Nano100B SDK|Version 2021.9.23||
|3|Nu-LB-NANO130|REV 1.2|Hardware|

### For ISP Test
|N|Name|Description|Note|
|---|---|---|---|
|1|UDOO Dual|with Android 6.0 - Marshmallow|Hardware|
|2|Nu-LB-NANO130|REV 1.2, with Bridge firmware|Hardware|
|3|NuTiny-SDK-Mini54FDE|V1.0, with ISP_UART firmware in LDROM|Hardware|

## System Overview(used Bridge firmware)
![image](https://user-images.githubusercontent.com/99227045/184124866-4104d5bf-9f39-47bf-970f-824cde9ae0a6.png)<br />
Your Android device and Nu-LB-NANO130(CON2) are connected by USB. Nu-LB-NANO130(PD0, PD1) and NuTiny-SDK-Mini54FDE(P12, P13) are connected by UART.

## How to ISP Firmware update on the Android(used Bridge firmware)
1. Please select that which item will try to connect USB you want in USB list. For your information, Vendor ID is 1046 and Product ID is 20512 of USB information for Bridge firmware.
![image](https://user-images.githubusercontent.com/99227045/184124921-5f52ae1c-4127-4f0e-943a-23847bdd469a.png)

2. Please approve about USB permission of app.
![image](https://user-images.githubusercontent.com/99227045/184124960-f7e2940a-6c9e-466a-81ab-9da7cae51f9f.png)

3. Please press the connect button. Then Bridge firmware will try to connect target board. After then target board must be reset. Then target board will be boot up from LDROM, it can be processing packet for ISP connect.
![image](https://user-images.githubusercontent.com/99227045/184125034-1308ca73-3371-4473-bb17-af369363015d.png)

4. If connect is successful, you can see the text of "USB Connected" and parts number. Please refer that if there no have your parts information in android app, it will be failed about connection. As you know, most embedded device maybe not enough resource. So I didn’t do add to this which parts information of all from Nuvoton. If you want add to your parts information, Please refer “How to add parts information in the Android app” in this document.
![image](https://user-images.githubusercontent.com/99227045/184125070-ccb84362-cb5b-4e57-9039-72c059720299.png)

5. Please select binary file for APROM of target board after press APROM button. And then please press the program button. Then file you selected will be program to APROM of target board.
![image](https://user-images.githubusercontent.com/99227045/184125114-c409609a-9db2-4fcd-98d6-e7cf6971a1c9.png)<br />
![image](https://user-images.githubusercontent.com/99227045/184125130-c8969468-62f5-44b7-9cd1-80a1ce107daf.png)<br />
![image](https://user-images.githubusercontent.com/99227045/184125160-6bc8c555-3413-4de5-9408-e856c07ddae8.png)

6. If you get alert message about successful, you can confirm that whether target board is work well after reset the target board.
![image](https://user-images.githubusercontent.com/99227045/184125191-412272c6-95c6-4f96-8f48-bbde5d2fca21.png)<br />
![image](https://user-images.githubusercontent.com/99227045/184125208-ef581410-c8d8-4906-9d4f-805400884b3e.png)

## Software Description
![image](https://user-images.githubusercontent.com/99227045/184125238-57debe6e-a78b-4263-ab53-725eec19bf54.png)
“NvtISPFragment.java” will handling for GUI and ISP. It will be send packets to Bridge firmware by the user's action(Like the click button). There have thread for ISP also. This thread will handling packets received from Bridge firmware. Connection and communication for USB HID is via "HidBridge.java". You can send packet via USB using WriteData function. There have thread for handling packets received from USB.<br />
Bridge firmware will handling packets of USB and UART. It will send packets received from Android app to target board via UART. Also it will send packets received from target board to Android app via USB.

## How to add parts information in the Android app
You can see "NvtChipInfo.java" in android app source tree. You can add parts information you want in this file. MAX_DEVICE_COUNT value is number of supported devices. Parts information will add to SupportDevicesList function. If you added new part information, you need to increase MAX_DEVICE_COUNT value. Parts information is stored as array. This array will use for searching parts ID by GetChipStaticInfo function later. Values for parts information is same as ISPTool provided by Nuvoton. You can refer link below.<br />
https://github.com/OpenNuvoton/ISPTool/blob/master/NuvoISP/DataBase/PartNumID.cpp

## Packet Description
Packet size is 64 Byte. There have 4 Byte of command list and 4 Byte of command index. You can ignore command index in the packet.<br />
![image](https://user-images.githubusercontent.com/99227045/187109385-1f5d628d-3b3e-4147-8a75-4212bc504247.png)

Please refer table below for command list.
|Command|Value(Hex/4 Byte)|Description|Note|
|---|---|---|---|
|CMD_UPDATE_APROM|0x000000A0|Update data to the internal flash of target board.||
|CMD_CONNECT|0x000000AE|Try connect to target board.||
|CMD_GET_DEVICEID|0x000000B1|Request device ID to target board.||
|CMD_UPDATE_SPIFLASH|0x000000D1|Update data to the external flash of target board.||

### CMD_CONNECT
Try connect to target board. You must send this packet to target board every few millisecond during reset for target board. Target board must be received this packet when boot-up.<br />
Host -> Target board<br />
![image](https://user-images.githubusercontent.com/99227045/187111015-b0d0b4f9-41f3-4b6d-ad1d-0f6625513afc.png)

If target board received this packet from Host, Target board will send packet same as CMD_CONNECT packet to Host.<br />
Target board -> Host<br />
![image](https://user-images.githubusercontent.com/99227045/187111015-b0d0b4f9-41f3-4b6d-ad1d-0f6625513afc.png)

### CMD_GET_DEVICEID
Request device ID to target board.<br />
Host -> Target board<br />
![image](https://user-images.githubusercontent.com/99227045/187118255-9322f814-989c-419e-869b-21a086fa748b.png)

If target board received this packet from Host, Target board will send packet include device ID to Host. You can refer to device ID for each device [here](https://github.com/OpenNuvoton/ISPTool/blob/master/NuvoISP/DataBase/PartNumID.cpp). <br />
Target board -> Host<br />
![image](https://user-images.githubusercontent.com/99227045/187118659-7463d6d0-6e21-4e1c-9325-b3697b31cb6a.png)

### CMD_UPDATE_APROM
Update data to the internal flash of target board. There have two kind of packet. If you send CMD_UPDATE_APROM packet to target board for first time, You must send packet include Start address and Total length like as picture below.<br />
Host -> Target board<br />
![image](https://user-images.githubusercontent.com/99227045/187119655-311e2f82-5130-4420-bb5e-7e56a664be7a.png)<br />
Start Address: This is start address that where you need write buffer data.<br />
Total Length: This is total length for buffer you needs write to flash.<br /><br />
You can send following packet after sent packet for first time.<br />
Host -> Target board<br />
![image](https://user-images.githubusercontent.com/99227045/187120873-208b2402-b0fa-4a24-a9e1-f0048a478bed.png)

If target board received this packet from Host, Target board will send packet include CRC for received packet to Host.<br />
Target board -> Host<br />
![image](https://user-images.githubusercontent.com/99227045/187121331-1cfb4a61-0a57-45a2-97bc-07ef03d73421.png)

### CMD_UPDATE_SPIFLASH
Update data to the external flash of target board.<br />
![image](https://user-images.githubusercontent.com/99227045/187121712-614a7957-8234-4a4f-8331-0065ee0831ed.png)<br />
Start Address: This is start address that where you need write buffer data.<br />
Write Length: Write buffer size.<br /><br />
If target board received this packet from Host, Target board will send packet include CRC for received packet to Host.<br />
Target board -> Host<br />
![image](https://user-images.githubusercontent.com/99227045/187121331-1cfb4a61-0a57-45a2-97bc-07ef03d73421.png)

## Thanks to
https://gist.github.com/ns50254/e5a265990eb8b145d04d<br />
https://github.com/OpenNuvoton/ISPTool<br />
https://github.com/OpenNuvoton/Nano100B_BSP/tree/master/SampleCode/StdDriver/USBD_HID_Transfer<br />
https://github.com/OpenNuvoton/Nano100B_BSP/tree/master/SampleCode/StdDriver/UART_TxRx_Function<br />
https://github.com/OpenNuvoton/Mini51BSP/tree/master/SampleCode/ISP/ISP_UART
