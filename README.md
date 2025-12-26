# SimpleTodoApp

A simple yet well-architected Android Todo application demonstrating **MVI (Model-View-Intent)** architecture with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room Database**, and **Hilt Dependency Injection**.

## Features

- **Todo Management** - Create, read, update, and delete todos
- **Mark Complete** - Toggle todos between complete and incomplete states
- **Search** - Filter todos by title or description
- **Tab Filtering** - View all todos or only completed ones
- **Undo Delete** - Restore accidentally deleted todos via Snackbar
- **Modal Bottom Sheets** - Modern Compose UI for adding, editing, and viewing todos

---

## Architecture

This project follows the **MVI (Model-View-Intent)** architectural pattern with **Jetpack Compose** for UI and reactive programming using **Kotlin Coroutines and Flow**.

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
│   ├── AppModule          → App-wide providers
│   ├── DatabaseModule     → Room & DAO providers
│   └── RepositoryModule   → Repository binding
├── domain/
│   ├── models/            → Todo (Domain model)
│   └── repository/        → TodoRepository (Repository interface)
├── navigation/
│   ├── NavRoutes.kt       → Type-safe navigation routes
│   └── TodoNavHost.kt     → Navigation host setup
├── ui/
│   ├── mvi/               → MVI framework (MviContract, MviViewModel, MviExtensions)
│   ├── theme/             → Material 3 theme (Color.kt, Theme.kt)
│   ├── preview/           → Preview utilities (DevicePreviews.kt)
│   ├── util/              → UI utilities (TestTags.kt, ModifierExtensions.kt)
│   ├── todolist/          → TodoListScreen, MviViewModel, Contract, TodoItem
│   ├── addedittodo/       → AddEditTodoScreen, MviViewModel, Contract
│   └── tododetail/        → TodoDetailScreen, MviViewModel, Contract
├── util/
│   └── Constants.kt       → App constants
├── MainActivity.kt        → Entry point with Compose setup
└── TodoApp.kt             → @HiltAndroidApp
```

### MVI Pattern

The app implements a custom MVI framework with three core components:

```kotlin
// Contract defines the screen's state, intents, and side effects
interface UiState           // Immutable screen state
interface UiIntent          // User actions/events
interface SideEffect        // One-time events (navigation, snackbars)

// Base ViewModel provides the MVI infrastructure
abstract class MviViewModel<State, Intent, Effect>(initialState: State) {
    val uiState: StateFlow<State>           // Observable state
    val effect: Flow<Effect>                // One-time effects

    fun onIntent(intent: Intent)            // Handle user actions
    protected fun updateState(reduce: State.() -> State)  // Pure state reducer
    protected suspend fun sendEffect(effect: Effect)      // Emit side effects
}
```

### Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER ACTION                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   VIEW (Compose Screen)                          │
│  • Captures user input                                           │
│  • Calls viewModel.onIntent(Intent)                              │
│  • Collects uiState via collectState()                           │
│  • Observes effects via ObserveAsEvents()                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MVI VIEWMODEL                               │
│  • Receives Intent via onIntent()                                │
│  • Updates state via updateState { copy(...) }                   │
│  • Emits side effects via sendEffect()                           │
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

### MVI Contract Example

Each screen defines its contract with State, Intent, and SideEffect:

```kotlin
// State - immutable representation of screen
@Immutable
data class TodoListState(
    val todos: List<Todo> = emptyList(),
    val filteredTodos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filter: TodoFilter = TodoFilter.ALL
) : UiState

// Intent - user actions
sealed interface TodoListIntent : UiIntent {
    data object LoadTodos : TodoListIntent
    data class UpdateSearchQuery(val query: String) : TodoListIntent
    data class ToggleComplete(val todo: Todo) : TodoListIntent
    data class DeleteTodo(val todo: Todo) : TodoListIntent
    data class UndoDelete(val todo: Todo) : TodoListIntent
    data class OpenTodoDetail(val todoId: Long) : TodoListIntent
    data object AddNewTodo : TodoListIntent
}

// SideEffect - one-time events
sealed interface TodoListSideEffect : SideEffect {
    data class ShowSnackbar(val message: String, val todo: Todo? = null) : TodoListSideEffect
    data class NavigateToDetail(val todoId: Long) : TodoListSideEffect
    data object NavigateToAddTodo : TodoListSideEffect
}
```

### MVI Extensions Usage

The `MviExtensions.kt` file provides Compose utilities for working with the MVI pattern:

#### `collectState()` - Lifecycle-Aware State Collection

```kotlin
@Composable
fun TodoListScreen(viewModel: TodoListMviViewModel = hiltViewModel()) {
    val state = viewModel.collectState()  // Pauses when app is in background

    TodoListContent(
        todos = state.filteredTodos,
        isLoading = state.isLoading
    )
}
```

#### `rememberOnIntent()` - Stable Intent Handler

```kotlin
@Composable
fun TodoListScreen(viewModel: TodoListMviViewModel = hiltViewModel()) {
    val state = viewModel.collectState()
    val onIntent = viewModel.rememberOnIntent()  // Stable lambda, won't cause recomposition

    TodoListContent(state = state, onIntent = onIntent)
}

@Composable
fun TodoListContent(state: TodoListState, onIntent: (TodoListIntent) -> Unit) {
    Button(onClick = { onIntent(TodoListIntent.AddNewTodo) }) {
        Text("Add Todo")
    }
}
```

#### `ObserveAsEvents()` - Side Effect Handling

```kotlin
@Composable
fun TodoListScreen(
    viewModel: TodoListMviViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe one-time side effects (lifecycle-aware)
    ObserveAsEvents(flow = viewModel.effect) { effect ->
        when (effect) {
            is TodoListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.todoId)
            is TodoListSideEffect.NavigateToAddTodo -> onNavigateToAdd()
            is TodoListSideEffect.ShowSnackbar -> {
                val result = snackbarHostState.showSnackbar(
                    message = effect.message,
                    actionLabel = effect.todo?.let { "Undo" }
                )
                if (result == SnackbarResult.ActionPerformed) {
                    effect.todo?.let { onIntent(TodoListIntent.UndoDelete(it)) }
                }
            }
        }
    }
}
```

#### `rememberEventChannel()` - Buffered Event Dispatch

```kotlin
@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    val debouncedSearch = rememberEventChannel(onSearch)  // Buffers rapid inputs

    TextField(onValueChange = { query -> debouncedSearch(query) })
}
```

#### Extensions Summary

| Function | Purpose | Prevents |
|----------|---------|----------|
| `collectState()` | Lifecycle-aware state collection | Memory leaks, background updates |
| `rememberOnIntent()` | Stable intent handler | Unnecessary recompositions |
| `rememberEventChannel()` | Buffered event dispatch | Rapid duplicate events |
| `ObserveAsEvents()` | One-time effect handling | Event re-delivery on config change |

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **MVI with StateFlow** | Unidirectional data flow with single source of truth |
| **Jetpack Compose** | Declarative UI with better recomposition control |
| **Type-Safe Navigation** | Compile-time route safety using Kotlin Serialization |
| **Kotlin Coroutines + Flow** | Modern async handling with structured concurrency |
| **Repository Pattern** | Clean abstraction over data sources, easier to test |
| **Domain Layer** | Separates business models from data layer entities |
| **Hilt DI** | Compile-time dependency injection with less boilerplate |
| **Channel for Effects** | Prevents re-delivery of one-time events on config change |

---

## Tech Stack

### Core Dependencies

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| Compose | compose-bom | 2024.12.01 | Compose Bill of Materials |
| Compose | activity-compose | 1.9.3 | Compose Activity integration |
| Compose | navigation-compose | 2.8.5 | Type-safe Compose navigation |
| Compose | hilt-navigation-compose | 1.2.0 | Hilt integration for Compose |
| Compose | lifecycle-runtime-compose | 2.8.7 | Lifecycle-aware Compose utilities |
| Core Android | androidx-core-ktx | 1.17.0 | Kotlin extensions for Android APIs |
| Core Android | androidx-appcompat | 1.7.1 | Backward compatibility support |
| Core Android | androidx-activity | 1.12.1 | Activity lifecycle management |
| UI | material3 | (via BOM) | Material Design 3 components |
| Database | room-runtime | 2.8.4 | SQLite abstraction layer |
| Database | room-ktx | 2.8.4 | Room + Coroutines/Flow integration |
| Async | kotlinx-coroutines-core | 1.9.0 | Kotlin coroutines |
| Async | kotlinx-coroutines-android | 1.9.0 | Android main thread dispatcher |
| Lifecycle | lifecycle-viewmodel-ktx | 2.8.7 | ViewModel with coroutine support |
| DI | hilt-android | 2.57.2 | Dependency injection framework |
| Serialization | kotlinx-serialization | 1.7.3 | Type-safe navigation arguments |
| Compat | desugar_jdk_libs | 2.1.5 | Java 8+ API support on older Android |

### Testing Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| junit | 4.13.2 | Unit testing framework |
| truth | 1.4.4 | Fluent assertions library (Google) |
| mockk | 1.13.13 | Kotlin-first mocking library |
| turbine | 1.2.0 | Flow testing library |
| kotlinx-coroutines-test | 1.9.0 | Coroutine testing utilities |
| robolectric | 4.14.1 | JVM-based Android framework simulation |
| compose-ui-test-junit4 | (via BOM) | Compose UI testing |
| compose-ui-test-manifest | (via BOM) | Compose test manifest |
| room-testing | 2.8.4 | In-memory Room database for tests |
| hilt-android-testing | 2.57.2 | Hilt support for tests |
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
| Compose Compiler | Kotlin 2.0+ integrated |

---

## Testing Strategy

### Testing Pyramid

```
              ┌───────────────┐
             │   UI Tests    │  ← Compose UI Tests (Instrumented)
            ┌┴───────────────┴┐
           │   Integration    │  ← Room DAO, Robolectric
          ┌┴─────────────────┴┐
         │     Unit Tests     │  ← MviViewModel, Repository
        └─────────────────────┘
```

### Unit Tests (`app/src/test`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoRepositoryTest.kt` | Repository delegation to DAO, mapping, error handling |
| `TodoEntityTest.kt` | Entity data class defaults, copy function, equality/hashCode |
| `TodoListMviViewModelTest.kt` | Intent handling, state mutations, side effects |
| `AddEditTodoMviViewModelTest.kt` | Save/load todo operations, form validation |
| `TodoDetailMviViewModelTest.kt` | Detail view loading, toggle complete |
| `TodoListContentTest.kt` | Compose UI rendering, state-driven updates |
| `AddEditTodoContentTest.kt` | Form UI, validation display |
| `TodoDetailContentTest.kt` | Detail screen rendering |
| `TodoItemComposeTest.kt` | Individual todo item composable |

### Instrumented Tests (`app/src/androidTest`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoDaoTest.kt` | CRUD operations with in-memory Room database |
| `MigrationTest.kt` | Database schema validation |

### Testing Patterns

| Pattern | Description |
|---------|-------------|
| **MockK** | Kotlin-friendly mocking with `relaxed = true` |
| **Turbine** | Flow testing with `test {}` block |
| **runTest** | Coroutine test dispatcher for deterministic testing |
| **ComposeTestRule** | Compose UI testing with semantics |
| **TestTags** | Centralized test identifiers for reliable queries |
| **TestTodoFactory** | Factory for creating consistent test data |
| **Robolectric** | JVM-based UI testing (faster than instrumented) |

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run unit tests with coverage report
./gradlew testDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.lutukai.simpletodoapp.ui.todolist.TodoListMviViewModelTest"
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
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── models/Todo.kt
│   │   │   │   │   └── repository/TodoRepository.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── NavRoutes.kt
│   │   │   │   │   └── TodoNavHost.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── mvi/
│   │   │   │   │   │   ├── MviContract.kt
│   │   │   │   │   │   ├── MviViewModel.kt
│   │   │   │   │   │   └── MviExtensions.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   └── Theme.kt
│   │   │   │   │   ├── preview/
│   │   │   │   │   │   └── DevicePreviews.kt
│   │   │   │   │   ├── util/
│   │   │   │   │   │   ├── TestTags.kt
│   │   │   │   │   │   └── ModifierExtensions.kt
│   │   │   │   │   ├── todolist/
│   │   │   │   │   │   ├── TodoListContract.kt
│   │   │   │   │   │   ├── TodoListMviViewModel.kt
│   │   │   │   │   │   ├── TodoListScreen.kt
│   │   │   │   │   │   └── TodoItem.kt
│   │   │   │   │   ├── addedittodo/
│   │   │   │   │   │   ├── AddEditTodoContract.kt
│   │   │   │   │   │   ├── AddEditTodoMviViewModel.kt
│   │   │   │   │   │   └── AddEditTodoScreen.kt
│   │   │   │   │   └── tododetail/
│   │   │   │   │       ├── TodoDetailContract.kt
│   │   │   │   │       ├── TodoDetailMviViewModel.kt
│   │   │   │   │       └── TodoDetailScreen.kt
│   │   │   │   ├── util/
│   │   │   │   │   └── Constants.kt
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
