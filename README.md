# QRCode-Generator
A QR Code Generator for currently only for Android, I built it by using python with kivy framework and using python for android with Buildozer.




A cross-platform QR code generator app built using Kivy and KivyMD. This app allows users to generate QR codes from input text and save or remove them on Android, Windows, and Linux platforms.

## Features
- Generate QR codes from user-input text.
- Save generated QR codes to a specific folder based on the platform (Android, Linux, or Windows).
- Remove or save generated QR codes.
- A simple, intuitive user interface with buttons for generating, saving, and removing QR codes.
- Cross-platform compatibility with Android, Linux, and Windows.

## Supported Platforms
- **Android**: Saves generated QR codes to the device's storage (`/sdcard/DCIM/QRCode`).
- **Linux**: Saves generated QR codes to the Desktop in the `QR_Generater` folder.
- **Windows**: Saves generated QR codes to the Desktop in the `QR_Generater` folder.

## Installation

To run this app, you need to have Python 3.6+ installed along with the required dependencies.

### Install dependencies:

```bash
pip install kivy, kivymd, qrcode
