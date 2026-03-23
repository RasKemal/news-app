# Spaceflight News Application

A persistent Android application built for the Spaceflight News API (v4). This project implements an offline-first architecture, automated data synchronization, and a responsive user interface.

## Architecture and Design Patterns
The application follows Clean Architecture principles, separating concerns into three distinct layers:

* **Data Layer:** Orchestrates data between the REST API and a local persistent store. It implements an offline-first strategy using the Paging 3 RemoteMediator pattern.
* **Domain Layer:** Contains pure Kotlin business logic, including UseCases and Repository interfaces. This layer is independent of the Android framework.
* **Presentation Layer:** Developed with Jetpack Compose and follows the MVVM pattern. It utilizes a strict Unidirectional Data Flow (UDF) to manage UI state and user interactions.

## Technical Implementation Details

### Networking and Synchronization
* **ETag Support:** The NetworkModule is configured with a dedicated OkHttp Disk Cache. This enables the system to handle conditional GET requests (ETags) automatically. If the server content has not changed, the app receives a 304 Not Modified response, reducing bandwidth consumption.
* **Cache Invalidation:** The RemoteMediator utilizes a 1-hour Time-to-Live (TTL) policy. Before triggering network requests, the system evaluates the age of the local cache to determine if a refresh is required.
* **Resource Separation:** Separate OkHttpClient instances are provided for API data and Image loading. This ensures that heavy image traffic does not interfere with critical metadata synchronization.

### Data Persistence and Search
* **Full-Text Search (FTS4):** The local database uses SQLite FTS4 virtual tables to provide keyword searching across article titles and summaries. Search functionality remains operational while the device is offline.
* **Transactional Integrity:** Cache resets during refresh signals are performed within Room Transactions. This prevents partial data states and ensures that user-favorited articles are preserved during cache invalidation.

### UI and State Management
* **Unidirectional Data Flow (UDF):** ViewModels expose immutable state via StateFlow, while UI components remain stateless and communicate user intents through lambda-based callbacks.
* **Centralized State Hoisting:** Hoisting to the MainViewModel and MainScreen manages navigation, responsive layouts, and user preferences. This centralized ownership ensures state consistency across different screen configurations.
* **Adaptive Navigation:** The MainScreen manages the hand-off logic between phone and tablet layouts. Hoisted navigation state ensures that the detail view remains synchronized during orientation changes or transitions between the standard backstack and the split-pane view.
* **Input Optimization:** Search queries are processed through a reactive pipeline utilizing a debounce and a filter. This optimizes resource usage by preventing redundant network and database operations during active user input.

## Testing Strategy
The project includes a test suite for verifying the integrity of each layer:
* **Paging Logic:** Verification of RemoteMediator load states and cache invalidation.
* **Data Layer:** Unit tests for JSON serialization and Room DAO logic using in-memory databases and MockWebServer.
* **Reactive State:** Verification of StateFlow and SharedFlow emission sequences using Turbine.
## Technology Stack
* **UI:** Jetpack Compose
* **Asynchrony:** Kotlin Coroutines & Flow
* **Dependency Injection:** Dagger Hilt
* **Database:** Room (FTS4 support)
* **Networking:** Retrofit & OkHttp
* **Pagination:** Paging 3 (RemoteMediator)
* **Storage:** Jetpack DataStore
* **Navigation:** Compose Navigation
* **Testing:** MockK, JUnit4, Turbine, Robolectric, MockWebServer

## Future Improvements

* **Current Implementation:** The application utilizes a passive cache invalidation strategy within the RemoteMediator.
* **Future Improvement:** A production-grade news application would benefit from proactive synchronization using **WorkManager**. Implementing a `PeriodicWorkRequest` would ensure the local database is populated before the user opens the app.
* **Note:** Background synchronization was scoped out of this implementation to prioritize architectural stability, UI responsiveness, and core Paging 3 logic.


## Prerequisites
* **Android Studio:** Ladybug (2024.2.1) or newer.
* **JDK:** Version 17.
* **Android SDK:** API Level 36 (Android 16) is required for the build environment.

