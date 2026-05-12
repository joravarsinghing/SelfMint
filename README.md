[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/ravaroj/habit-currency-android/actions) [![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE) [![Latest Release](https://img.shields.io/badge/release-v2.00.01-blue)](https://github.com/ravaroj/habit-currency-android/releases/latest)

# SelfMint 


<!-- BEGIN SELF-MINT DESCRIPTION -->
<table border="0">
  <tr>
    <td style="border: none;" width="20%">
      <img src="SelfMint v2 Logo.png" style="max-width: 100%; height: auto;">
    </td>
    <td style="border: none;">
      <b>SelfMint</b> is a gamified productivity app that lets you create your own “behavioral economy”: complete tasks to earn currency, then spend that currency on rewards you define.
    </td>
  </tr>
</table>
<!-- END SELF-MINT DESCRIPTION -->

---
## 📸 Screenshots

<p align="center">
  <img src="screenshots/tasks.jpg" width="24%">
  <img src="screenshots/rewards.jpg" width="24%">
  <img src="screenshots/dashboard.jpg" width="24%">
  <img src="screenshots/tags.jpg" width="24%">
</p>

---

## 🚀 Features & Usage

SelfMint empowers you to build a personal economy around your habits and goals. Here's what you can do:

*   **Tasks (One-Time + Daily)**:
    *   **Daily Habits**: Tasks that regenerate automatically each day, helping you build consistent routines.
    *   **One-Time Tasks**: For specific goals or chores; these carry forward if not completed, ensuring nothing is forgotten.
    *   **Repeatable Completions**: Earn currency multiple times for daily tasks (tracked via `claimCount`), reinforcing positive behaviors.

*   **Rewards (One-Time + Daily)**:
    *   **Redeem with Currency**: Spend your earned currency on rewards you define.
    *   **Daily Rewards**: Stay available after redemption, perfect for recurring treats.
    *   **One-Time Rewards**: Disappear once redeemed, ideal for special, infrequent indulgences.

*   **Flexible & Forgiving**:
    *   **Reversible Actions**: Uncomplete tasks, undo redemptions, and edit flows ensure your wallet balance remains accurate and consistent.
    *   **Daily Rollover**: The app intelligently processes day changes on startup, regenerating daily tasks and cleaning up old history.

*   **Insightful Tracking**:
    *   **Dashboard**: Visualize your earning and spending activity, and track your wallet's trend over time.
    *   **Tags**: Organize your tasks with colored tags (reward tag support is in progress) for better categorization and focus.

---

## ✨ Unique Selling Points

*   **Personalized Economy**: Define your own tasks, rewards, and currency values to create a system that truly motivates you.
*   **Flexible Tracking**: Adapt to your life with both one-time and daily tasks/rewards, plus the ability to undo actions.
*   **Visual Progress**: The dashboard provides clear insights into your productivity and financial habits within the app.

---

## ⬇️ Download (APK)

You can find signed builds on GitHub Releases.

*   Go to the [latest release](https://github.com/ravaroj/habit-currency-android/releases/latest) and download the APK.
*   Install on your Android device (you may need to allow “install unknown apps” for your browser/files app).

---

## 🛠️ Tech Stack

*   Kotlin, Jetpack Compose (Material 3)
*   Room, DataStore
*   Coroutines / Flow
*   Navigation Compose

---

## 👨‍💻 Development

1.  Clone the repository: `git clone https://github.com/ravaroj/habit-currency-android.git`
2.  Open the project in Android Studio (Ladybug or newer recommended).
3.  Sync Gradle and run on an emulator or physical device.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 🤝 Contributing

We welcome contributions! If you're interested in improving SelfMint, please consider:

*   Reporting bugs
*   Suggesting new features
*   Submitting pull requests

Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for more details (coming soon).

---

## 🔒 Privacy, Security, & Permissions

*   **Privacy**: SelfMint is designed to be a local-first application. No personal data is collected or transmitted externally. All your data resides solely on your device.
*   **Security**: The app does not handle sensitive personal information or require internet permissions, minimizing security risks.
*   **Permissions**: SelfMint requires minimal permissions to function. (List specific permissions here if applicable, e.g., "Storage access for backups").

 This repo currently still contains some legacy naming from an earlier project title (“Habit Currency”), including the internal package name `com.ravaroj.habitcurrency`. 