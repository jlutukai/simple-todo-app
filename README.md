# SimpleTodoApp

A simple yet well-architected Android Todo application demonstrating **MVP (Model-View-Presenter)** architecture with **RxJava 3**, **Room Database**, and **Hilt Dependency Injection**.

## Features

- **Todo Management** - Create, read, update, and delete todos
- **Mark Complete** - Toggle todos between complete and incomplete states
- **Search** - Filter todos by title
- **Tab Filtering** - View all todos or only completed ones
- **Undo Delete** - Restore accidentally deleted todos via Snackbar
- **Bottom Sheet Dialogs** - Modern UI for adding, editing, and viewing todos

---

## Architecture

This project follows the **MVP (Model-View-Presenter)** architectural pattern with reactive programming using **RxJava 3**.

### Project Structure

```
com.lutukai.simpletodoapp/
├── data/
│   ├── local/
│   │   ├── dao/           → TodoDao (Room DAO with RxJava)
│   │   ├── database/      → AppDataBase (Room Database)
│   │   └── entity/        → TodoEntity (Data model)
│   └── repository/        → TodoRepository (Data abstraction layer)
├── di/
│   ├── DatabaseModule     → Room & DAO providers
│   └── RepositoryModule   → SchedulerProvider binding
├── ui/
│   ├── base/              → BaseView, BasePresenter (MVP contracts)
│   ├── todolist/          → TodoListFragment, Presenter, Contract, Adapter
│   ├── addedittodo/       → AddEditTodoDialog, Presenter, Contract
│   └── tododetail/        → TodoDetailDialog, Presenter, Contract
├── util/
│   └── schedulerprovider/ → SchedulerProvider interface & AppSchedulerProvider
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
│  • Delegates to Presenter                                        │
│  • Displays results                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTER                                 │
│  • Handles business logic                                        │
│  • Manages RxJava subscriptions (CompositeDisposable)            │
│  • Updates View state (showLoading, showTodos, showError)        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       REPOSITORY                                 │
│  • Abstracts data source                                         │
│  • Applies schedulers (subscribeOn IO, observeOn UI)             │
│  • Returns RxJava types (Flowable, Single, Maybe, Completable)   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ROOM DAO + DATABASE                          │
│  • SQLite operations via Room                                    │
│  • Reactive queries with RxJava integration                      │
└─────────────────────────────────────────────────────────────────┘
```

### MVP Contract Pattern

The app uses a Contract interface pattern to define the communication between View and Presenter:

```kotlin
interface TodoListContract {

    interface View : BaseView {
        fun showTodos(todos: List<TodoEntity>)
        fun showEmpty()
        fun showTodoDeleted(todo: TodoEntity)
        fun navigateToAddTodo()
        fun navigateToTodoDetail(todoId: Long)
    }

    interface Presenter : BasePresenter<View> {
        fun loadTodos()
        fun addNewTodo()
        fun openTodoDetail(todo: TodoEntity)
        fun toggleComplete(todo: TodoEntity)
        fun deleteTodo(todo: TodoEntity)
        fun undoDelete(todo: TodoEntity)
    }
}
```

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **MVP over MVVM** | Explicit view-presenter communication with clear separation of concerns |
| **RxJava 3** | Reactive streams for async operations and automatic threading management |
| **Repository Pattern** | Clean abstraction over data sources, easier to test and extend |
| **Hilt DI** | Compile-time dependency injection with less boilerplate than Dagger |
| **SchedulerProvider** | Abstraction over RxJava schedulers enables easy testing |

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
| Database | room-rxjava3 | 2.8.4 | Room + RxJava 3 integration |
| Reactive | rxjava | 3.1.12 | Reactive extensions for Java |
| Reactive | rxandroid | 3.0.2 | RxJava bindings for Android (main thread scheduler) |
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
         │     Unit Tests     │  ← Presenter, Repository, Entity
        └─────────────────────┘
```

### Unit Tests (`app/src/test`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoRepositoryTest.kt` | Repository delegation to DAO, RxJava error handling, scheduler verification |
| `TodoEntityTest.kt` | Entity data class defaults, copy function, equality/hashCode |
| `TodoListPresenterTest.kt` | List loading, filtering, delete/undo, UI state sequences |
| `AddEditTodoPresenterTest.kt` | Save/load todo operations, form validation |
| `TodoDetailPresenterTest.kt` | Detail view loading, toggle complete functionality |
| `TodoAdapterTest.kt` | DiffUtil `areItemsTheSame` and `areContentsTheSame` logic |
| `TodoListFragmentTest.kt` | Fragment UI visibility with Robolectric |
| `AddEditTodoDialogTest.kt` | Dialog ADD/EDIT modes, input field states |
| `TodoDetailDialogTest.kt` | Detail dialog UI element visibility |

### Instrumented Tests (`app/src/androidTest`)

| Test File | Coverage Area |
|-----------|---------------|
| `TodoDaoTest.kt` | CRUD operations with in-memory Room database, Flowable emissions on data changes |
| `MigrationTest.kt` | Database schema validation, column existence verification |

### Testing Patterns

| Pattern | Description |
|---------|-------------|
| **MockK** | Kotlin-friendly mocking with `relaxed = true` for flexible test setup |
| **Trampoline Scheduler** | Synchronous RxJava execution for deterministic tests |
| **verifyOrder{}** | UI state sequence validation (showLoading → hideLoading → showContent) |
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
│   │   │   │   │   └── repository/TodoRepository.kt
│   │   │   │   ├── di/
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── base/
│   │   │   │   │   │   ├── BasePresenter.kt
│   │   │   │   │   │   └── BaseView.kt
│   │   │   │   │   ├── todolist/
│   │   │   │   │   │   ├── TodoAdapter.kt
│   │   │   │   │   │   ├── TodoListContract.kt
│   │   │   │   │   │   ├── TodoListFragment.kt
│   │   │   │   │   │   └── TodoListPresenter.kt
│   │   │   │   │   ├── addedittodo/
│   │   │   │   │   │   ├── AddEditTodoContract.kt
│   │   │   │   │   │   ├── AddEditTodoDialog.kt
│   │   │   │   │   │   └── AddEditTodoPresenter.kt
│   │   │   │   │   └── tododetail/
│   │   │   │   │       ├── TodoDetailContract.kt
│   │   │   │   │       ├── TodoDetailDialog.kt
│   │   │   │   │       └── TodoDetailPresenter.kt
│   │   │   │   ├── util/schedulerprovider/
│   │   │   │   │   ├── AppSchedulerProvider.kt
│   │   │   │   │   └── SchedulerProvider.kt
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

## License

This project is for educational purposes.
