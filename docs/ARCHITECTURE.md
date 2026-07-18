# NyaaAndroid — Architecture

This document explains how NyaaAndroid is put together: the layers, the data flow,
and the HTML-scraping strategy that stands in for Nyaa's (non-existent) public API.

---

## 1. High-level overview

NyaaAndroid is a single-Activity, Jetpack-Compose app that follows a pragmatic
**MVVM** layering:

```
┌─────────────┐   user intent    ┌──────────────┐   suspend call   ┌────────────────┐
│  Compose UI │ ───────────────► │  ViewModel   │ ───────────────► │   Repository   │
│  (screens)  │                  │  (UiState)   │                  │                │
│             │ ◄─────────────── │              │ ◄─────────────── │                │
└─────────────┘   UiState flow   └──────────────┘   domain models  └───────┬────────┘
                                                                           │
                                                          ┌────────────────┴───────────────┐
                                                          │                                 │
                                                   ┌──────▼───────┐                 ┌────────▼────────┐
                                                   │ ApiService   │                 │  Room database  │
                                                   │ (Retrofit)   │                 │ (favorites +    │
                                                   │  → raw HTML  │                 │  saved searches)│
                                                   └──────┬───────┘                 └─────────────────┘
                                                          │
                                                   ┌──────▼───────┐
                                                   │ NyaaHtmlParser│  Jsoup → domain models
                                                   └──────────────┘
```

Key idea: **Nyaa.si has no JSON API.** The app requests the same HTML pages a browser
would, then parses them with Jsoup into clean Kotlin data classes. Everything above
the `network` package is API-shape-agnostic — it only ever deals with `TorrentUI` /
`TorrentDetail`.

---

## 2. Layers

### 2.1 `model/` — domain data
Plain immutable data classes, framework-free:

| Type | Purpose |
|---|---|
| `TorrentUI` | One row in a search/listing result (title, size, seeders, category, `isTrusted`/`isRemake`…). |
| `TorrentDetail` | The full torrent page (hash, submitter, description, comments, file tree, magnet, `.torrent` link…). |
| `Comment` | A single comment (user, date, content, avatar). |
| `TorrentFile` | A node in the torrent's file tree (recursive `children`). |

### 2.2 `network/` — remote access + parsing
* **`NyaaApiService`** — a Retrofit interface returning `ResponseBody` (raw HTML).
  The listing endpoint maps directly onto Nyaa's query parameters (see §3).
* **`NyaaNetwork`** — the Retrofit singleton (`BASE_URL = https://nyaa.si/`).
* **`NyaaHtmlParser`** — a stateless object that turns HTML into domain models:
  * `parseTorrents(html)` → `List<TorrentUI>` (reads `table.torrent-list` rows).
  * `parseDetail(html)` → `TorrentDetail` (reads the panel, comments, and file list).
  * `parseRecursive(ul)` → walks the nested `<ul>/<li>` structure into a `TorrentFile` tree.

### 2.3 `data/` — repositories & persistence
* **`TorrentRepository`** — the single entry point for remote torrent data. Runs the
  network call + Jsoup parse on `Dispatchers.IO` so ViewModels stay on the main thread.
* **`FavoriteRepository`** — wraps the Room DAOs for favorites and saved searches.
* **`data/local/entity/`** — the Room `NyaaDatabase`, its DAOs (`FavoriteDao`,
  `SavedSearchDao`), entities (`FavoriteTorrent`, `SavedSearch`) and `Migrations`.

### 2.4 `ui/` — presentation
* **`screens/`** — one package per screen (`home`, `detail`, `favorites`, `settings`),
  each with a `Screen` composable and a `ViewModel`. ViewModels expose a sealed
  `UiState` (`Loading` / `Success` / `Error`) via Compose `mutableStateOf` or `StateFlow`.
* **`components/`** — reusable widgets: `AdvancedSearchDialog`, `TorrentItem`,
  `TorrentList`, `FileTreeView`, `MarkdownText`, `CommentItem`, `BadgeInfo`, etc.
* **`helpers/`** — `CategoryHelper` (category → colour/icon) and the custom scrollbar.
* **`theme/`** — Material 3 `Theme`, `Color`, `SemanticColors` (seeder/leecher/favorite
  accents exposed through `NyaaTheme.colors`), and `Type`.

### 2.5 `utils/`
* **`ThemePreferences`** — persists the selected `AppTheme` (Light/Dark/AMOLED/System)
  in Jetpack DataStore.

---

## 3. Nyaa query parameters

The listing endpoint (`GET /`) is driven entirely by query params. `NyaaApiService`
mirrors them one-to-one:

| Param | Field | Meaning |
|---|---|---|
| `q` | `query` | Free-text keywords. |
| `c` | `category` | Category code `main_sub`, e.g. `1_2` (Anime – English), `0_0` = all. |
| `f` | `filter` | **Quality filter**: `0` = none, `1` = no remakes, `2` = trusted only. |
| `u` | `user` | Restrict to one uploader (`user:` search). |
| `p` | `page` | 1-based page number. |
| `s` | `sort` | `id` (date), `size`, `seeders`, `leechers`, `downloads`. |
| `o` | `order` | `asc` / `desc`. |

The detail page is fetched with `@Url` since Nyaa gives an absolute `/view/{id}` link.

### Trusted / remake rows
Nyaa colour-codes each listing row with a Bootstrap contextual class:
`tr.success` = trusted uploader, `tr.danger` = remake. `parseRow` reads these into
`TorrentUI.isTrusted` / `isRemake`, and `TorrentItem` renders a coloured card border
plus a small badge.

### `.torrent` download
The detail page exposes both a `magnet:` link and a relative `.torrent` URL
(e.g. `/download/1234.torrent`). `DetailScreen.normalizeTorrentUrl` resolves the
relative form to an absolute `https://nyaa.si/...` URL, then hands it to the system
via `ACTION_VIEW` so the browser / download manager can save it.

---

## 4. Data flow example — a search

1. User adjusts keywords, category, sort, order and quality filter in
   `AdvancedSearchDialog` and taps **Search**.
2. The dialog calls back into `HomeViewModel.onSearch(query, category, sort, order, filter)`,
   which stores the criteria and calls `loadTorrents()`.
3. `loadTorrents()` sets `uiState = Loading`, then asks
   `TorrentRepository.getTorrents(...)` on `Dispatchers.IO`.
4. The repository calls `NyaaApiService.getTorrentsHtml(...)`, gets raw HTML, and runs
   `NyaaHtmlParser.parseTorrents(...)`.
5. On success the ViewModel publishes `Success(List<TorrentUI>)`; `HomeScreen`
   recomposes `TorrentList`. On failure it publishes `Error(message)`.

Favorites are independent: `FavoriteRepository.allFavorites` is a Room `Flow` collected
as Compose state, so toggling a heart updates every screen reactively.

---

## 5. Threading & state

* Network + parsing always run on `Dispatchers.IO` (inside the repository).
* ViewModels expose UI state on the main thread via `mutableStateOf` (Home/Detail) or
  `StateFlow` with `SharingStarted.WhileSubscribed` (favorites, saved searches).
* Compose observes that state and recomposes; there is no manual thread hopping in the UI.

---

## 6. Build notes

* **Toolchain:** Gradle 9.6.1, Android Gradle Plugin 9.0.1, Kotlin 2.2.10, Java 17.
* **JDK:** JDK 17 is the minimum. Gradle 9.6 also runs on JDK 26; Kotlin simply falls
  back to a JVM_24 target with a warning and builds fine, so no `JAVA_HOME` override is
  required on a JDK 26 host.
* **Built-in Kotlin:** AGP 9 compiles Kotlin itself, so there is no `kotlin-android`
  plugin. Compiler options live in a top-level `kotlin { compilerOptions { … } }` block.
  Because KSP still registers its generated sources through the Kotlin sourceSets DSL,
  `android.disallowKotlinSourceSets=false` is set in `gradle.properties` until KSP
  migrates to the `android.sourceSets` DSL.
* Room schemas are generated via KSP (`kspDebugKotlin`), pinned to `2.2.10-2.0.2` to
  match the Kotlin version.
* No API keys or secrets are needed — the app talks to public Nyaa pages only.

---

## 7. Extending the app

Some natural next steps, roughly ordered by effort:

* **Sukebei toggle** — the sister site `sukebei.nyaa.si` shares the exact HTML layout;
  a switchable `BASE_URL` would unlock it.
* **Infinite scroll** — replace the prev/next pager in `TorrentList` with append-on-scroll.
* **RSS import** — Nyaa exposes RSS feeds per search; could feed a "watch" feature.
* **Full localization** — `AdvancedSearchDialog` still has a few hard-coded French
  labels ("Tous", "Décroissant"…) that should move to `strings.xml`.
