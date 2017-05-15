# Savior_bleAntiLost
E6765 IoT - Group DCBA

These are the source code for the Android Studio Project of an Anti-lost Device named Savior.

1. ANTI Mode: check folder "app"
    - Able to scan the bluetooth devices and get the information including MAC addresses and RSSI values.
    - Able to convert the RSSI values into real distances.
    - Able to keep tracking of the selected device (which serves as the item need to be prevent from getting lost), and vibrate for the lost situation.

2. LOCATE Mode: check folder "app2" and also file "read_gpsData.js"
    - Able to get the real-time geo information from GPS module, and store them in the DynamoDB (after data cleaning) by using the timestamp as Primary Key.
    - Able to fetch the most recent information according to the timestamp from DynamoDB into the app, and use them to locate on the Google Map.

3. Hardware parts are:
    - Bluefruit LE Sniffer - Bluetooth Low Energy (BLE 4.0) - nRF51822 - v3.0
    - Adafruit Ultimate GPS Breakout - 66 channel w/10 Hz updates
 
Please repeat this project by replace "***" with the corresponding information, like identical pool ID.
