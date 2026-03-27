# 🐾 NyaaAndroid

A modern, high-performance native Android client for **Nyaa.si**, built with **Kotlin 2.0** and **Jetpack Compose**.  
Designed for speed, readability, and a seamless mobile experience.

---

## ✨ Features

### 📖 Advanced Markdown Rendering
* **Smart Hybrid Tables**: Automatically detects content types. Data tables stay horizontal, while screenshot-heavy tables are transformed into **vertical lists** for better mobile visibility.
* **Interactive Image Gallery**: One-tap to zoom. Click any image within a description to open a high-resolution full-screen preview.
* **Sanitized Content**: Advanced regex cleaning to handle Nyaa's specific Markdown quirks, BBCode remnants, and broken pipe structures.

### 🔍 Discovery & Search
* **Deep Filtering**: Browse by categories (Anime, Audio, Literature, Live Action, etc.) with precise sub-category support.
* **Powerful Search**: Supports advanced queries (e.g., `user:username` or specific keywords).
* **Seamless Pagination**: Fast and fluid navigation through thousands of torrents.

### ⚡ Torrent Interaction
* **One-Tap Magnet**: Direct integration to launch your favorite torrent client (Flud, LibreTorrent, etc.).
* **Favorites System**: Save your must-watch torrents locally for quick access.
* **Share Integration**: Instantly share torrent links with your friends.
* **File Browser**: Full tree-view of torrent contents before you even start the download.

### 🎨 Premium UI/UX
* **Material 3 Design**: Fully compliant with the latest Android design standards (Material You).
* **Multi-Theme Support**:
    * **Light Mode** (Clean & Crisp)
    * **Dark Mode** (Soft Grey)
    * **AMOLED Mode** (True Black for battery saving and visual comfort)
* **Dynamic Visuals**: Category-specific Kanji badges and color-coded health indicators (Seeds/Leechers).

---

## 🛠 Tech Stack

* **Language**: [Kotlin 2.0](https://kotlinlang.org/) (The latest & greatest)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Architecture**: MVVM (Model-View-ViewModel)
* **Markdown Engine**: [Markwon](https://github.com/noties/Markwon) (Extensively customized)
* **Image Loading**: [Coil](https://coil-kt.github.io/coil/) (With custom Span interceptors)
* **Networking**: [Jsoup](https://jsoup.org/) for high-speed HTML parsing
* **Dependency Injection**: Manual / ViewModel Factories

---

## 📦 Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/Nagutos/NyaaAndroid.git](https://github.com/Nagutos/NyaaAndroid.git)
