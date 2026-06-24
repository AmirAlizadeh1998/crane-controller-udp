Crane Controller via UDP 🏗️

A real-time Android application for controlling industrial machinery (cranes) over a local network using the UDP protocol. This project focuses on low-latency communication and a clean, intuitive user interface.

🎬 Demo

https://github.com/user-attachments/assets/03b13d2d-1018-49a7-9ceb-a13144e8e83f

📖 About The Project

This application was developed to provide a modern, mobile-first alternative to traditional physical controllers for industrial cranes. The primary goal was to achieve minimal latency (< 50ms) for precise and safe operations using the lightweight UDP protocol. It connects to a hardware endpoint (the crane’s control unit) on a specified IP address and port.
✨ Features

    Real-time Control: Send movement commands (Up, Down, Left, Right, etc.) instantly.
    UDP Communication: Lightweight and fast protocol for time-sensitive commands.
    Simple UI: A clean interface built with Jetpack Compose for easy operation.
    Configurable Connection: Easily set the target IP address and Port in the settings.
    Connection Status: Visual feedback to show if the app is successfully connected to the hardware.

🛠️ Tech Stack & Architecture

    Language: Kotlin
    UI: Jetpack Compose
    Architecture: MVVM (Model-View-ViewModel)
    Asynchronous: Kotlin Coroutines for network operations.
    Networking: Raw UDP Sockets (java.net.DatagramSocket)

🚀 Getting Started

Since this project requires specific hardware, a mock server is provided in the mock_server directory to simulate the crane’s control unit. This allows anyone to run the app and observe the UDP communication without access to the actual machinery.
Prerequisites

    Android Studio Iguana | 2023.2.1 or newer
    JDK 17
    Python 3.x (to run the mock server)

1. Running the Android App

    Clone the repository:

    git clone https://github.com/AmirAlizadeh1998/crane-controller-udp.git

    Open the project in Android Studio.
    Let Gradle sync and build the project.
    Run the app on an Android Emulator or a physical device.

2. Running the Mock Server

    Open a terminal or command prompt in the project’s root directory.
    Navigate to the mock server’s directory:
   
    cd mock_server

    Run the Python script. The server will start and print its IP address.
   
    python mock_crane_server.py

    In the Android app, enter the IP address and Port (8888) shown in the server’s console.
    Press the control buttons in the app and watch the commands appear in the server’s console!

🧠 Challenges & Key Learnings

    Implementing UDP for Real-Time Control: Chose UDP for its speed and low overhead, which is critical for real-time applications. This involved understanding the trade-off between speed and the potential for packet loss on less stable networks.
    Asynchronous Programming with Kotlin Coroutines: Managed all network operations on a background thread using Dispatchers.IO to ensure a non-blocking, responsive UI.
    Declarative UI with Jetpack Compose: Designed and built a simple, uncluttered, and state-driven UI suitable for an industrial environment, focusing on usability and clear feedback for the operator.

⚠️ Disclaimer

This project is a proof-of-concept and is intended for educational purposes only. DO NOT use this application to control real, live machinery without extensive testing, safety features, and professional validation. The author is not responsible for any damage or harm caused by the use of this software.

📬 Contact

Amir Alizadeh - www.linkedin.com/in/amirhossein-alizadeh-dev - amirhossein.alizadeh.work@gmail.com
