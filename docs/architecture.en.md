# TaskBridge Architecture

TaskBridge is a Kotlin Multiplatform sample app that keeps business logic in a shared Core module and presents it through thin native Android and iOS applications. The project demonstrates shared domain models, reactive state, platform capability handlers, and platform-specific UI layers.

## Modules

| Module | Responsibility |
| --- | --- |
| `Core` | Shared Kotlin Multiplatform logic: models, services, use cases, stories, interactors, storage, networking, and event buses. |
| `Android` | Native Android app built with Jetpack Compose. It adapts Core interactors into Android repositories and renders screens. |
| `iOS` | Native SwiftUI app. It adapts Core interactors into Swift repositories and exposes Core flows as Swift async streams. |
| `Platform_Handlers` | Platform-facing handler module for capabilities that Core cannot implement directly on every platform. |
| `docs` | Static documentation and mock API data, including task template JSON used by the templates flow. |

## High-Level Flow

```text
Android Compose UI / SwiftUI
        |
Platform repositories
        |
Core interactors
        |
Use cases
        |
Stories
        |
Stateful and stateless services
        |
Storage, network clients, platform handlers, event buses
```

The UI does not talk to storage, networking, or platform notification APIs directly. It works through repositories, and repositories delegate to Core interactors.

## Core Entry Point

`TaskBridge` is the public entry point into shared logic. Platform apps create it with:

- `PlatformDependencies`, used for platform-specific infrastructure such as database creation.
- `CorePlatformHandlers`, used for capabilities that need native implementations, such as reminders.

After creation, platform code obtains interactors from `TaskBridge`:

- `navigationInteractor()`
- `templatesInteractor()`
- `tasksInteractor()`
- `remindersInteractor()`

The entry point also exposes `eventEmitter`, which lets platform handlers notify Core about platform-side changes.

## Composition

`CoreAssembler` is the internal composition root. It owns:

- `CoreServiceLocator`, which lazily creates and keeps services.
- `UseCaseContainer`, which resolves public-to-Core use cases.
- `UserStoriesContainer`, which creates smaller internal story operations.
- `CoreEventBuses` and `CoreEventEmitter`, which connect platform events back into shared logic.

This keeps construction details inside Core while exposing a compact interactor API to Android and iOS.

## Core Layers

### Interactors

Interactors are the stable platform-facing facade. They expose flows and suspend functions that platform repositories can call without knowing how Core is assembled internally.

### Use Cases

Use cases coordinate domain operations and translate service data into platform-friendly state models. Examples include navigation state observation, task operations, template loading, and reminder management.

### Stories

Stories are small internal operations. They wrap focused actions such as loading tasks, replacing a task subtree, selecting a tab, loading a remote resource, or scheduling a reminder.

### Services

Services own long-lived state or process requests:

- Stateful services use reactive state and command processing.
- Stateless services process requests through a mailbox-style sequential loop.
- `AppStateService` owns navigation state.
- `TasksService` owns task tree state and persists changes through `TaskStorageManager`.
- `RemindersService` synchronizes Core reminder state with platform reminder handlers.
- `NetworkService` performs JSON network requests.
- `RemoteResourceService` stores remote resource state, loading errors, timestamps, TTL metadata, and in-flight request information.

## Data and Capabilities

Tasks are stored locally through a shared Room database abstraction in Core. Platform-specific database builders are supplied through `PlatformDependencies`.

Task templates are loaded from JSON through the shared network layer. The current template URL points at `docs/mock-api/templates.json` in the GitHub repository, and Core applies a TTL before reloading it.

Reminders are platform capabilities. Core defines the shared reminder model and orchestration, while Android and iOS provide native reminder handlers. Platform handlers report updates back to Core through `CoreEventEmitter`.

## Platform Layers

### Android

The Android app initializes `TaskBridge` in `MainActivity`, creates Android repository implementations around Core interactors, and renders screens with Jetpack Compose. Compose screens observe Kotlin flows and call repository methods for user actions.

### iOS

The iOS app initializes `TaskBridge` in `TaskBridgeApp`, creates Swift repository implementations around Core interactors, injects them through SwiftUI environment values, and converts Core flows into `AsyncStream` values for SwiftUI view models.

## Design Intent

The architecture keeps domain decisions in shared Kotlin code while allowing each platform to keep a native UI and native integrations. Core owns the rules, state transitions, storage orchestration, network loading, and event coordination. Android and iOS stay focused on presentation, lifecycle, and platform APIs.
