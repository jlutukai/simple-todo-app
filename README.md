# SimpleTodoApp

A simple yet well-architected Android Todo application demonstrating **MVVM (Model-View-ViewModel)** architecture with **Kotlin Coroutines & Flow**, **Room Database**, and **Hilt Dependency Injection**.

## Features

- **Todo Management** - Create, read, update, and delete todos
- **Mark Complete** - Toggle todos between complete and incomplete states
- **Search** - Filter todos by title
- **Tab Filtering** - View all todos or only completed ones
- **Undo Delete** - Restore accidentally deleted todos via Snackbar
- **Bottom Sheet Dialogs** - Modern UI for adding, editing, and viewing todos

---

## Architecture

This project follows the **MVVM (Model-View-ViewModel)** architectural pattern with reactive programming using **Kotlin Coroutines and Flow**.

### Project Structure

```
com.lutukai.simpletodoapp/
├── data/
│   ├── local/
│   │   ├── dao/           → TodoDao (Room DAO with Flow & suspend functions)
│   │   ├── database/      → AppDataBase (Room Database)
│   │   └── entity/        → TodoEntity (Data model)
│   ├── mapper/            → ToDomain.kt, ToEntity.kt (Entity ↔ Domain mapping)
│   └── repository/        → TodoRepositoryImpl (Repository implementation)
├── di/
│   ├── DatabaseModule     → Room & DAO providers
│   └── RepositoryModule   → Repository binding
├── domain/
│   ├── models/            → Todo (Domain model)
│   └── repository/        → TodoRepository (Repository interface)
├── ui/
│   ├── todolist/          → TodoListFragment, ViewModel, UiState, Adapter
│   ├── addedittodo/       → AddEditTodoDialog, ViewModel, UiState
│   └── tododetail/        → TodoDetailDialog, ViewModel, UiState
├── util/
│   └── ViewExtensions.kt  → UI utility extensions
├── MainActivity.kt
└── TodoApp.kt             → @HiltAndroidApp
```

### Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER ACTION                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    VIEW (Fragment/Dialog)                        │
│  • Captures user input                                           │
│  • Calls ViewModel methods                                       │
│  • Observes StateFlow for UI state                               │
│  • Collects Events for one-time actions                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        VIEWMODEL                                 │
│  • Holds UI state (MutableStateFlow → StateFlow)                 │
│  • Emits one-time events (Channel → Flow)                        │
│  • Launches coroutines in viewModelScope                         │
│  • Calls repository suspend functions                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DOMAIN LAYER (Interface)                       │
│  • TodoRepository interface                                      │
│  • Defines contract for data operations                          │
│  • Returns Flow for reactive streams, suspend for one-shot ops   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DATA LAYER (Implementation)                    │
│  • TodoRepositoryImpl                                            │
│  • Maps Entity ↔ Domain models                                   │
│  • Delegates to Room DAO                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ROOM DAO + DATABASE                          │
│  • SQLite operations via Room                                    │
│  • Flow queries for reactive updates                             │
│  • Suspend functions for CRUD operations                         │
└─────────────────────────────────────────────────────────────────┘
```

### StateFlow + Channel Pattern

The app uses StateFlow for UI state and Channel for one-time events:

```kotlin
// UiState - represents screen state
data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events - one-time actions
sealed class TodoListEvent {
    data class ShowSnackbar(val message: String, val todo: Todo) : TodoListEvent()
    data class NavigateToDetail(val todoId: Long) : TodoListEvent()
    data object NavigateToAddTodo : TodoListEvent()
}

// ViewModel exposes state and events
class TodoListViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private val _events = Channel<TodoListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
}
```

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **MVVM with StateFlow** | Lifecycle-aware state management with single source of truth |
| **Kotlin Coroutines + Flow** | Modern async handling with structured concurrency |
| **Repository Pattern** | Clean abstraction over data sources, easier to test and extend |
| **Domain Layer** | Separates business models from data layer entities |
| **Hilt DI** | Compile-time dependency injection with less boilerplate |
| **Channel for Events** | Prevents re-delivery of one-time events on configuration change |

---

## Tech Stack

### Core Dependencies

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| Core Android | androidx-core-ktx | 1.17.0 | Kotlin extensions for Android APIs |
| Core Android | androidx-appcompat | 1.7.1 | Backward compatibility support |
| Core Android | androidx-activity | 1.12.1 | Activity lifecycle management |
| Core Android | androidx-constraintlayout | 2.2.1 | Flexible UI layout system |
| UI | material | 1.13.0 | Material Design 3 components |
| Database | room-runtime | 2.8.4 | SQLite abstraction layer |
| Database | room-ktx | 2.8.4 | Room + Coroutines/Flow integration |
| Async | kotlinx-coroutines-core | 1.9.0 | Kotlin coroutines |
| Async | kotlinx-coroutines-android | 1.9.0 | Android main thread dispatcher |
| Lifecycle | lifecycle-viewmodel-ktx | 2.8.7 | ViewModel with coroutine support |
| Lifecycle | lifecycle-runtime-ktx | 2.8.7 | Lifecycle-aware coroutine scopes |
| DI | hilt-android | 2.57.2 | Dependency injection framework |
| Navigation | navigation-fragment | 2.9.6 | Fragment-based navigation |
| Navigation | navigation-ui | 2.9.6 | Navigation UI integration |
| Compat | desugar_jdk_libs | 2.1.5 | Java 8+ API support on older Android versions |

### Testing Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| junit | 4.13.2 | Unit testing framework |
| truth | 1.4.4 | Fluent assertions library (Google) |
| mockk | 1.13.13 | Kotlin-first mocking library |
| turbine | 1.2.0 | Flow testing library |
| kotlinx-coroutines-test | 1.9.0 | Coroutine testing utilities |
| robolectric | 4.14.1 | JVM-based Android framework simulation |
| espresso-core | 3.7.0 | UI testing framework |
| room-testing | 2.8.4 | In-memory Room database for tests |
| hilt-android-testing | 2.57.2 | Hilt support for tests |
| fragment-testing | 1.8.5 | Fragment testing utilities |
| androidx-test-core | 1.6.1 | Core test utilities |
| androidx-test-runner | 1.6.2 | AndroidJUnit test runner |

### Build Configuration

| Setting | Value |
|---------|-------|
| Min SDK | 23 (Android 6.0 Marshmallow) |
| Target SDK | 36 |
| Compile SDK | 36 |
| Java/Kotlin | JVM 17 |
| Build System | Gradle with Kotlin DSL |
| Annotation Processor | KSP (Kotlin Symbol Processing) |

---

## Testing Strategy

### Testing Pyramid

```
              ┌───────────────┐
             │   UI Tests    │  ← Espresso (Instrumented)
            ┌┴───────────────┴┐
           │   Integration    │  ← Room DAO, Robolectric
          ┌┴─────────────────┴┐
         │     Unit Tests     │  ← ViewModel, Repository, Entity
        └─────────────────────┘
```

### Unit Tests (`app/src/test`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoRepositoryTest.kt` | Repository delegation to DAO, mapping, error handling |
| `TodoEntityTest.kt` | Entity data class defaults, copy function, equality/hashCode |
| `TodoListViewModelTest.kt` | List loading, filtering, delete/undo, state emissions |
| `AddEditTodoViewModelTest.kt` | Save/load todo operations, form validation |
| `TodoDetailViewModelTest.kt` | Detail view loading, toggle complete functionality |
| `TodoAdapterTest.kt` | DiffUtil `areItemsTheSame` and `areContentsTheSame` logic |
| `TodoListFragmentTest.kt` | Fragment UI visibility with Robolectric |

### Instrumented Tests (`app/src/androidTest`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoDaoTest.kt` | CRUD operations with in-memory Room database, Flow emissions on data changes |
| `MigrationTest.kt` | Database schema validation, column existence verification |

### Testing Patterns

| Pattern | Description |
|---------|-------------|
| **MockK** | Kotlin-friendly mocking with `relaxed = true` for flexible test setup |
| **Turbine** | Flow testing with `test {}` block for collecting and asserting emissions |
| **runTest** | Coroutine test dispatcher for deterministic async testing |
| **In-memory Room DB** | Real database operations without persistence for integration tests |
| **HiltTestRunner** | Custom test runner for dependency injection in tests |
| **launchFragmentInHiltContainer** | Custom extension for fragment testing with Hilt |

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run unit tests with coverage report
./gradlew testDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.lutukai.simpletodoapp.data.repository.TodoRepositoryTest"
```

---

## Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17**
- **Android SDK 36**

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/yourusername/SimpleTodoApp.git

# Navigate to project directory
cd SimpleTodoApp

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Run the App

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select a device or emulator
4. Click **Run** (or press `Shift + F10`)

---

## Project Files Reference

```
SimpleTodoApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lutukai/simpletodoapp/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/TodoDao.kt
│   │   │   │   │   │   ├── database/AppDataBase.kt
│   │   │   │   │   │   └── entity/TodoEntity.kt
│   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── ToDomain.kt
│   │   │   │   │   │   └── ToEntity.kt
│   │   │   │   │   └── repository/TodoRepositoryImpl.kt
│   │   │   │   ├── di/
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── models/Todo.kt
│   │   │   │   │   └── repository/TodoRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── todolist/
│   │   │   │   │   │   ├── TodoAdapter.kt
│   │   │   │   │   │   ├── TodoListFragment.kt
│   │   │   │   │   │   ├── TodoListViewModel.kt
│   │   │   │   │   │   └── TodoListUiState.kt
│   │   │   │   │   ├── addedittodo/
│   │   │   │   │   │   ├── AddEditTodoDialog.kt
│   │   │   │   │   │   ├── AddEditTodoViewModel.kt
│   │   │   │   │   │   └── AddEditTodoUiState.kt
│   │   │   │   │   └── tododetail/
│   │   │   │   │       ├── TodoDetailDialog.kt
│   │   │   │   │       ├── TodoDetailViewModel.kt
│   │   │   │   │       └── TodoDetailUiState.kt
│   │   │   │   ├── util/ViewExtensions.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── TodoApp.kt
│   │   │   └── res/
│   │   ├── test/                    ← Unit tests
│   │   └── androidTest/             ← Instrumented tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml           ← Version catalog
└── build.gradle.kts
```

---
