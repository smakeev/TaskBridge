# TaskBridge Core Entities

This document describes the entities in the TaskBridge Core module.

## Core Entry Point
### TaskBridge
The main entry point for the Core module. It provides access to public Interactors and the `CoreEventEmitter`.
- `TEMPLATES_URL`: Constant URL for fetching task templates.
- `TEMPLATES_TTL_MILLIS`: Constant TTL for task templates caching.
- `eventEmitter`: Entry point for platform events.

---

## Events (Core to Platform Bridge)
Internal and public components for event-driven communication.

### CoreEventEmitter
Public facade for emitting events from platforms into Core.
- `remindersUpdated(reminders: List<Reminder>)`: Notifies Core about platform reminder list changes.

### CoreEventBus (Internal)
A generic, thread-safe event bus implementation using `MutableSharedFlow`.

### CoreEventBuses (Internal)
Container for all internal event buses (e.g., `reminderEvents`).

---

## Interactors (Platform Layer)
High-level bridges used by the platform to interact with the Core.

### NavigationInteractor
Public class providing a simplified interface for navigation logic. Assembled by the `CoreAssembler`. Platform-side implementations (like `NavigationRepository`) should ensure proper lifecycle management of the exposed Flows/Streams to prevent leaks.
- `activePath`: `Flow<NavigationPath?>`
- `currentTab`: `Flow<AppTab>`
- `overlay`: `Flow<NavigationOverlay?>`
- `selectTab(tab: AppTab)`: Selects the specified tab.
- `fetchActivePath()`: Suspends and returns the current path.
- `fetchCurrentTab()`: Suspends and returns the current tab.
- `fetchOverlay()`: Suspends and returns the current overlay.

### TemplatesInteractor
Public platform-facing interactor for managing task templates.
- `templatesState`: `Flow<TaskTemplatesState>`
- `loadTemplates()`: Triggers loading if cache is expired.
- `forceLoadTemplates()`: Forcefully reloads templates.

### TasksInteractor
Public platform-facing interactor for managing tasks.
- `tasksState`: `Flow<TasksState>`
- `loadTasks()`: Loads tasks from storage.
- `createTask(task: TaskItem)`: Persists a new task tree.
- `replaceTask(task: TaskItem)`: Replaces an existing task's subtree.
- `deleteTaskTree(taskId: TaskId)`: Recursively deletes a task and its descendants.

---

## Composition
Internal components for dependency management.

### CoreAssembler (Internal)
The composition root that holds the service locator and containers. It is responsible for assembling Interactors with their required Use Cases.

### CoreServiceLocator (Internal)
Manages the lifecycle of internal services. Lazily creates and keeps service instances.

### UserStoriesContainer (Internal)
Creates internal User Stories via explicit getters.

### UseCaseContainer (Internal)
Creates public-to-core Use Cases. Provides access by type via a generic `get` method.

---

## User Stories (Internal)
### GetAppStateServiceStory
Retrieves the `AppStateService` instance.

### SelectTabStory
Orchestrates tab selection via `AppStateService`.

### SubscribeToNavigationStory
Provides a `Flow<NavigationState>` derived from the `AppStateService`.

### SubscribeToActivePathStory
Provides a `Flow<NavigationPath?>` derived from the navigation state.

### SubscribeToOverlayStory
Provides a `Flow<NavigationOverlay?>` derived from the navigation state.

---

## Remote Resource Stories (Internal)
Generic stories for managing remote resources by URL.

### LoadRemoteResourceStory
Loads a remote resource using a provided loader (delegating to `NetworkService`) and updates `RemoteResourceService`. Respects TTL.

### ForceLoadRemoteResourceStory
Same as `LoadRemoteResourceStory` but ignores TTL.

### ObserveRemoteResourceStory
Provides a `Flow<RemoteResourceEntry?>` for a specific URL, allowing observation of its loading status, data, and errors.

---

## Tasks Stories (Internal)
Internal atomic operations over the `TasksService`.

### LoadTasksStory
Triggers a fresh load of all tasks from local storage.

### CreateTaskStory
Persists a new task tree through the `TasksService`.

### ReplaceTaskStory
Replaces an existing task's subtree via the `TasksService`.

### DeleteTaskTreeStory
Recursively deletes a task and all its descendants.

### ObserveTasksStory
Provides a reactive `Flow` of the current `TasksServiceData`.

---

## Use Cases (Internal to Core)
### SelectTabUseCase
Handles tab selection logic.
- `selectTab(tab: AppTab)`: Selects the specified application tab.

### NavigationStateObserverUseCase
Observes or fetches navigation-related state.
- `subscribeOnActivePath()`: Returns a `Flow<NavigationPath?>`.
- `subscribeOnCurrentTab()`: Returns a `Flow<AppTab>`.
- `subscribeOnOverlay()`: Returns a `Flow<NavigationOverlay?>`.
- `fetchActivePath()`: Suspends until the first `NavigationPath?` is available.
- `fetchCurrentTab()`: Suspends until the first `AppTab` is available.
- `fetchOverlay()`: Suspends until the first `NavigationOverlay?` is available.

### TaskTemplatesUseCase
Internal use case for managing task templates. Maps generic remote resource state to domain-specific templates state.
- `loadTemplates()`: Triggers loading using `TEMPLATES_URL` and `TEMPLATES_TTL_MILLIS`.
- `forceLoadTemplates()`: Forcefully reloads templates.
- `observeTemplatesResource()`: Returns a `Flow<RemoteResourceEntry?>`.

### TasksUseCase
Internal use case for managing tasks. Orchestrates task stories to provide domain-level operations.
- `loadTasks()`: Triggers task loading.
- `createTask(task: TaskItem)`: Triggers task creation.
- `replaceTask(task: TaskItem)`: Triggers task replacement.
- `deleteTaskTree(taskId: TaskId)`: Triggers task subtree deletion.
- `observeTasks()`: Returns a `Flow<TasksServiceData>`.

---

## Services
### Service
Base interface for all services.

### BaseStatefulService (Internal)
Abstract base class for stateful services using the Actor pattern.

### StatefulService
Interface for services with reactive state and command processing.

### AppStateService (Internal)
Manages the application state, including navigation.

### TasksService (Internal)
Stateful service that owns the task tree state. It persists changes through `TaskStorageManager` and ensures deterministic behavior by reloading state from storage after mutations. Supported operations include loading, creating, replacing subtrees, and deleting subtrees.

### NetworkService (Internal)
Stateless service for network operations. Wraps `JsonRequestManager` and provide sequential handling for JSON loading requests. Does not cache results.

### RemoteResourceService (Internal)
Generic stateful service for managing remote resource state by URL. Orchestrates loading logic using provided lambdas and stores data, errors, and TTL metadata. Supports in-flight request deduplication and TTL-based caching.

### ServiceRequest
A marker interface for requests that can be processed by stateless services.

### StatelessService
A generic interface for services that process requests and return results without maintaining long-living state.

### BaseStatelessService (Internal)
Abstract base class for stateless services. It implements a sequential request processing loop using a mailbox pattern and provides deferred responses to callers. It skips processing for requests that were cancelled while in the mailbox.

---

## Data Models

### Navigation
#### AppTab
Enum for primary navigation tabs (TASKS, TEMPLATES, REMINDERS).

#### NavigationState
Root navigation state model.

#### NavigationDestination
Sealed interface for navigation screens.

#### NavigationPath
Represents a navigation stack.

#### NavigationOverlay
Models temporary overlays (Alerts, Sheets, etc.).

### Templates
#### TemplateId
A value class wrapping a `String` to represent a unique identifier for a template.

#### TemplateTaskType
An enum representing the possible types of tasks within a template (CHECKBOX, PROGRESS, CONTAINER).

#### TemplateTaskItem
A recursive data class representing a task item within a template. It can contain child tasks, forming a tree structure.

#### TaskTemplate
A data class representing a full task template, including its metadata and the root of its task tree.

#### TaskTemplatesState
Public state model for task templates, including the template list, loading status, and error messages.

### Tasks
#### TaskId
A value class representing a unique identifier for a task.

#### TaskType
An enum representing the three mutually exclusive task kinds: `CHECKBOX`, `PROGRESS`, and `CONTAINER`.

#### TaskProgress
A value class for tracking progress (0 to 100).

#### TaskItem
The central task domain model. It supports recursive parent-child structures for `CONTAINER` tasks via the `children` list. It also includes an explicit `parentId` relation to support persistence, identity linkage, and future tree operations. Completion state (`isCompleted`) is derived based on the task type (e.g., containers are completed only if all their children are completed).

#### TasksState
Public state model for tasks, including the task tree list, loading status, and error messages.

### Reminders
#### ReminderId
A value class representing a unique identifier for a reminder.

#### ReminderType
An enum representing the type of reminder: `START` (exact time) or `DEADLINE` (relative to a deadline).

#### Reminder
The central reminder domain model. Reminders are standalone objects containing notification content and trigger timing. Platform-specific handlers will later be responsible for scheduling and canceling real notifications based on these models.

#### ReminderEvent (Internal)
A sealed interface representing incoming events from platform reminder handlers.
- `RemindersUpdated(reminders: List<Reminder>)`: Emitted when the full list of reminders is replaced (e.g., on startup, after scheduling, or after cancellation).

---

## Handlers (Platform to Core)
Shared interfaces implemented by platforms and injected into Core.

### ReminderHandler
Public interface for notification scheduling and reminder retrieval. Platforms implement this to provide real notification capabilities.
- `getAllReminders()`: Retrieves the current scheduled reminders.
- `scheduleReminder(reminder)`: Schedules a platform notification.
- `cancelReminder(id)`: Cancels a scheduled notification.
- Handlers are wired with `CoreEventEmitter` to notify Core of changes.

### CorePlatformHandlers
Container for all platform handlers, passed to `TaskBridge` during initialization.

---

## Storage (Internal)
Internal components for persistence.

### TaskEntity
A Room entity for persisting tasks. It uses flat relational storage with a `parentId` to represent tree relationships and a `sortOrder` for sibling ordering.

### TaskDao
Room Data Access Object for task operations, including CRUD and reactive observation via `Flow`.

### TaskDatabase
The main Room database class for the TaskBridge Core.

### TaskStorageManager
A task-specific manager that encapsulates Room details and uses the `TaskEntityMapper` to bridge between flat entities and recursive domain trees. It ensures full atomicity by using `database.withTransaction`. To prevent stale orphan descendants, `upsertTaskTree` and `upsertTaskTrees` explicitly replace the entire subtree for each affected root task. User-visible deletion recursively removes the entire task subtree to maintain data integrity independently of the underlying SQLite configuration.

### TaskEntityMapper
Responsible for flattening the recursive `TaskItem` tree into a list of `TaskEntity` for storage and reconstructing the tree back into domain models for the application.

---

## Network DTOs
Internal Data Transfer Objects used for serialization.

### TaskTemplateDto
A serializable version of `TaskTemplate`. Provides a `toDomain()` mapping function.

### TemplateTaskItemDto
A serializable version of `TemplateTaskItem`. Provides a `toDomain()` mapping function.

### TemplateTypeDto
A serializable enum for `TemplateTaskType`. Supports custom serial names for JSON compatibility.

---

## Network Managers (Internal)
Internal components for handling network operations.

### HttpJsonClient
Low-level JSON GET client using Ktor. Performs raw network requests and deserialization.

### JsonRequestManager
Internal manager for deduplicating in-flight JSON requests by URL. It ensures that concurrent requests to the same URL share the same execution and automatically cleans up after completion. Does not cache completed results.
