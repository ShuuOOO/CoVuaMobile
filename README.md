♟️ CoVuaMobile – Chess Mobile Application

Android chess application developed using Java and Android Studio, implementing standard chess rules with a clean and intuitive mobile interface.

🚀 How to Run the Application (Deployment Guide)
1️⃣ Requirements

Before running the project, ensure that your environment meets the following requirements:

Android Studio (recommended: latest stable version)

Java Development Kit (JDK) 11

Android SDK

Minimum SDK: API 21 (Android 5.0)

Target SDK: API 35

Android Emulator or Physical Android Device

2️⃣ Clone the Repository
git clone https://github.com/ShuuOOO/CoVuaMobile.git
cd CoVuaMobile

3️⃣ Open Project in Android Studio

Open Android Studio

Select Open an existing project

Navigate to the CoVuaMobile directory

Click OK and wait for Gradle Sync to complete

⏳ The project uses Gradle Wrapper (gradlew), so no manual Gradle installation is required.

4️⃣ Build Configuration

The application is configured with the following settings:

Language: Java

Minimum SDK: 21

Target SDK: 35

Compile SDK: 35

Java Compatibility: Java 11

These configurations are defined in:

app/build.gradle

5️⃣ Run the Application
Option A: Using Android Emulator

Open AVD Manager in Android Studio

Create or start a virtual device (API 21+)

Click Run ▶️ or press Shift + F10

Option B: Using Physical Device

Enable Developer Options and USB Debugging on your Android device

Connect the device via USB

Select the device and click Run ▶️

The application will be installed and launched automatically.

6️⃣ Application Flow

MainActivity: Entry point of the application

MainMenuActivity: Main menu navigation

LearnActivity: Learning or gameplay screen

GameController: Handles chess game logic

Model Layer:

ChessBoard: Board state and piece management

Move: Chess move representation

The project follows Object-Oriented Programming (OOP) principles to separate UI, controller, and game logic.

7️⃣ Common Issues & Fixes

🔧 Gradle Sync Failed

Make sure JDK 11 is selected:

File → Settings → Build Tools → Gradle → Gradle JDK

🔧 SDK Version Error

Install required SDK versions via:

Tools → SDK Manager

🔧 App Not Launching

Try:

Build → Clean Project

Build → Rebuild Project

📂 Project Structure (Overview)
app/
 ├── src/main/java/com/example/appcovua
 │   ├── MainActivity.java
 │   ├── MainMenuActivity.java
 │   ├── LearnActivity.java
 │   ├── controller/GameController.java
 │   └── model/
 │       ├── ChessBoard.java
 │       └── Move.java
 ├── res/layout/
 │   ├── activity_main.xml
 │   ├── main_menu.xml
 │   └── activity_learn.xml
 └── AndroidManifest.xml
