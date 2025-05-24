# Todo List Android Application

## Project Overview
A simple yet comprehensive Todo List application for Android, built with Kotlin. It allows users to manage their tasks effectively with features like due dates, reminders, sorting, and filtering. Data is persisted locally using the Room persistence library.

## Features
- Core CRUD operations for todo items (add, view, edit, delete).
- Data persistence using Room database.
- UI for main list, add/edit screen, and individual items.
- Marking items as complete (with visual strikethrough).
- Sorting items by title, creation date, or completion status.
- Filtering items by completion status (all, active, completed).
- Due dates for todo items, including UI for setting them.
- Reminder notifications for due dates using AlarmManager, BroadcastReceiver, and system notifications.
- Runtime permission requests for notifications (POST_NOTIFICATIONS).
- Checks and user guidance for exact alarm permissions.
- Clean codebase with string resources and basic accessibility considerations.

## Setup Instructions

Follow these steps to get the project set up on your local machine:

1.  **Prerequisites:**
    *   **Android Studio:** Ensure you have the latest stable version of Android Studio installed. You can download it from [developer.android.com/studio](https://developer.android.com/studio).
    *   **Android SDK:** Android Studio typically manages SDK installations. Make sure you have an SDK platform installed (e.g., Android API 33 (Tiramisu) or API 34 (Upside Down Cake)). You can manage SDKs via Android Studio's SDK Manager.
    *   **JDK (Java Development Kit):** Android Studio usually comes with an embedded JDK. If not, or if you have specific JDK needs, ensure JDK 11 or higher is installed and configured.

2.  **Get the Code:**
    *   **Clone the repository:**
        ```bash
        git clone <repository_url>
        ```
        (Replace `<repository_url>` with the actual URL of this repository).
    *   **Alternatively, download the ZIP:** If you have the project as a ZIP file, extract it to your desired location.

3.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select "Open" (or "Open an Existing Project").
    *   Navigate to the directory where you cloned or extracted the `TodoListApp` project (the root folder containing `settings.gradle`).
    *   Click "Open". Android Studio will then import the project and build it using Gradle. This might take a few minutes, especially on the first open, as it downloads dependencies.

4.  **Gradle Sync:**
    *   Once the project is open, Android Studio should automatically perform a Gradle sync. If not, you can trigger it manually by clicking "Sync Project with Gradle Files" (often an elephant icon with a sync arrow) in the toolbar.
    *   Ensure the Gradle sync completes successfully. Check the "Build" output window for any errors.

## Running the App

Once the project is successfully set up and Gradle sync is complete, you can run the application:

1.  **Select a Run Configuration:**
    *   In Android Studio's toolbar, you should see a dropdown menu for run configurations (it usually defaults to "app"). Ensure "app" is selected.

2.  **Choose a Device:**
    *   Next to the run configuration dropdown, you'll see a device dropdown menu. You can:
        *   **Run on an Android VirtualDevice (AVD):**
            *   If you have AVDs set up, select one from the list.
            *   If not, you can create one via "Tools" > "AVD Manager". Click "Create Virtual Device", select a hardware profile, a system image (e.g., Tiramisu API 33), and finish the setup. Once created, it should appear in the device dropdown.
        *   **Run on a Physical Device:**
            *   Connect your Android device to your computer via USB.
            *   Enable "Developer Options" and "USB Debugging" on your device. (You can find instructions online by searching "enable developer options android [your device model]").
            *   Your device should appear in the device dropdown. You might need to approve an authorization dialog on your device.

3.  **Run the App:**
    *   Click the "Run 'app'" button (the green play icon ▶️) in the toolbar.
    *   Android Studio will build the app, install it on the selected device/emulator, and then launch it.
    *   You can see the build progress in the "Build" window and any device logs in the "Logcat" window.

4.  **Using the App:**
    *   Once launched, you can interact with the Todo List application:
        *   Add new tasks using the Floating Action Button.
        *   Tap tasks to edit them.
        *   Mark tasks as complete using the checkbox.
        *   Swipe tasks to delete them.
        *   Use the options menu (top-right) to sort and filter tasks.
        *   Set due dates and times for tasks to receive reminder notifications.

## Troubleshooting

### Gradle Version Error

**Error Message:**
You might encounter an error message similar to:
```
Minimum supported Gradle version is 8.2. Current version is 7.4. If using the gradle wrapper, try editing the distributionUrl in ...\TodoListApp\gradle\wrapper\gradle-wrapper.properties to gradle-8.2-all.zip
```

**Cause:**
This means the Android Gradle Plugin version used in the project requires Gradle version 8.2 or higher, but your environment is attempting to use an older version.

**Solution:**
The project is configured to use Gradle 8.2 via the Gradle wrapper. The necessary wrapper files, especially `TodoListApp/gradle/wrapper/gradle-wrapper.properties`, should be included in the project.

1.  **Check for `gradle-wrapper.properties`:**
    Ensure the file `TodoListApp/gradle/wrapper/gradle-wrapper.properties` exists.

2.  **Create/Update `gradle-wrapper.properties`:**
    If the file is missing or incorrect, create or replace it with the following content:
    ```properties
    distributionBase=GRADLE_USER_HOME
    distributionPath=wrapper/dists
    distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-all.zip
    zipStoreBase=GRADLE_USER_HOME
    zipStorePath=wrapper/dists
    ```
    *(Note: You might need to create the `TodoListApp/gradle/wrapper/` directories if they don't exist).*

3.  **Alternative (If Gradle is installed system-wide):**
    If you have Gradle installed on your system, you can try navigating into the `TodoListApp` directory in your terminal and running `gradle wrapper --gradle-version 8.2`. This command can help generate or update the Gradle wrapper files. However, ensuring the `gradle-wrapper.properties` file has the correct `distributionUrl` as shown above is the most direct fix.

4.  **Refresh Gradle Project:**
    After ensuring the `gradle-wrapper.properties` file is correct, refresh your project in Android Studio (e.g., via "File" > "Sync Project with Gradle Files").
