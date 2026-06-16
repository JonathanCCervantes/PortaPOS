[PortaPOS_README.md](https://github.com/user-attachments/files/28985065/PortaPOS_README.md)
# PortaPOS 🛒📱

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin%2FJava-blue)
![Status](https://img.shields.io/badge/Status-In%20Development-orange)

**PortaPOS** is a lightweight, secure, and mobile Point of Sale (POS) Android application designed for small businesses, pop-up shops, and on-the-go merchants. It provides essential sales tracking features while ensuring device security and data integrity.

---

## ✨ Features

*   **Secure Kiosk Mode (Display Lock):** Locks the device to the PortaPOS app using Android's Screen Pinning feature. Users cannot navigate away or access other apps without entering the secure App PIN.
*   **Secure Sales Reset:** Administrators can safely clear or reset the daily sales summary, strictly gated behind a PIN authorization process to prevent accidental or malicious data loss.
*   **Sales Export & Printing:** 
    *   **Download:** Export the daily sales summary as a CSV or PDF file directly to the device's storage for accounting purposes.
    *   **Print:** Print receipts and sales summaries directly to supported thermal or standard printers via the Android `PrintManager` API.
*   **Intuitive UI:** Designed for fast-paced environments, allowing quick entry and checkout.

---

## 🛠️ Tech Stack & Architecture

*   **IDE:** Android Studio
*   **Language:** Kotlin / Java
*   **Architecture:** MVVM (Model-View-ViewModel) recommended
*   **Database:** Room Database (SQLite) for local data persistence
*   **APIs:** Android `PrintManager`, Storage Access Framework (SAF), `startLockTask()`

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Latest stable version recommended)
*   Android SDK API Level 24 or higher (Adjust based on your `build.gradle`)
*   An Android physical device or Emulator for testing.

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/JonathanCCervantes/PortaPOS.git
    ```
2.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Click on **File > Open** and select the cloned `PortaPOS` directory.
3.  **Sync Gradle:**
    *   Wait for Android Studio to index the project and sync the Gradle files. If prompted, download any missing SDK platforms or build tools.
4.  **Run the App:**
    *   Connect your Android device via USB (with USB Debugging enabled) or start an Emulator.
    *   Click the green **Run** button (`Shift + F10`).

---

## 🔒 Security Notes

*   **App PIN:** The default PIN configuration (if applicable) should be changed upon first deployment.
*   **Kiosk Mode:** For true Kiosk mode in a commercial environment, it is recommended to set this app as a *Device Owner* using Android Enterprise provisions so the user cannot easily bypass the screen pinning.

---

## 🤝 Contributing
*(Instructions for future team members or open-source contributors can go here).*

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License
This project is proprietary. All rights reserved by [Jonathan Cervantes](https://github.com/JonathanCCervantes). 
*(Update this section if you plan to use an open-source license like MIT or Apache 2.0)*
