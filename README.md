#  Crane Controller via UDP 🏗️

A real-time Android application for controlling industrial machinery (cranes) over a local network using the UDP protocol. This project focuses on low-latency communication and a clean, intuitive user interface.

## 🎬 Demo

https://github.com/user-attachments/assets/03b13d2d-1018-49a7-9ceb-a13144e8e83f

## 📖 About The Project

This application was developed to provide a modern, mobile-first alternative to traditional physical controllers for industrial cranes. The primary goal was to achieve minimal latency (`< 50ms`) for precise and safe operations using the lightweight UDP protocol. It connects to a hardware endpoint (the crane's control unit) on a specified IP address and port.

## ✨ Features

- **Real-time Control**: Send movement commands (Up, Down, Left, Right, etc.) instantly.
- **UDP Communication**: Lightweight and fast protocol for time-sensitive commands.
- **Simple UI**: A clean interface built with Jetpack Compose for easy operation.
- **Configurable Connection**: Easily set the target IP address and Port in the settings.
- **Connection Status**: Visual feedback to show if the app is successfully connected to the hardware.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronous**: Kotlin Coroutines for network operations.
- **Networking**: Raw UDP Sockets (java.net.DatagramSocket)

## 🚀 Getting Started

Since this project requires specific hardware, a **mock server** is provided to simulate the crane's control unit. You can run the app and see the UDP packets being received by the server.

### Prerequisites

- Android Studio Iguana | 2023.2.1 or newer
- JDK 17

### Installation & Running the App

1.  **Clone the repository:**
```sh
git clone https://github.com/AmirAlizadeh1998/crane-controller-udp.git

    Open the project in Android Studio.
    Let Gradle sync and build the project.
    Run the app on an Android Emulator or a physical device.

Running the Mock Server (Optional but Recommended)

(اگه یه سرور شبیه‌ساز ساده با پایتون یا نود جی‌اس بنویسی که فقط پکت‌های UDP رو بگیره و توی کنسول چاپ کنه، پروژه‌ت ۱۰۰ برابر حرفه‌ای‌تر می‌شه. اینجوری ریکروتر می‌تونه واقعا تستش کنه.)

    Navigate to the mock_server directory.
    Run the Python script:

                                                                    sh
python mock_crane_server.py

    In the Android app, set the IP address to your computer’s local IP and the port to the one the server is listening on.
    Press the control buttons in the app and watch the commands appear in the server’s console!

🧠 Challenges & Key Learnings

In this section, I learned about:

    Why UDP over TCP?: I chose UDP for its speed and low overhead, which is critical for real-time control. I had to consider how to handle potential packet loss, even though it’s rare on a stable local Wi-Fi network.
    Managing Network Operations on Android: Using Kotlin Coroutines (Dispatchers.IO) to handle network calls without blocking the main UI thread was a key part of this project.
    Building a Clean UI: Designing a simple and uncluttered UI with Jetpack Compose that is easy to use in an industrial environment.

⚠️ Disclaimer

This project is a proof-of-concept and is intended for educational purposes only. DO NOT use this application to control real, live machinery without extensive testing, safety features, and professional validation. The author is not responsible for any damage or harm caused by the use of this software.
📬 Contact

Amir Alizadeh - LinkedIn Profile - your.email@example.com
