# TaskBridge Core Entities

This document describes the entities in the TaskBridge Core module.

## Core Entry Point
### TaskBridge
The main entry point for the Core module. It provides access to public Interactors (e.g., `navigationInteractor()`). Internal Use Case access is hidden from the platform.
- `TEMPLATES_URL`: Constant URL for fetching task templates.
- `TEMPLATES_TTL_MILLIS`: Constant TTL for task templates caching.

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
- `observeTemplates()`: Returns a `Flow<TaskTemplatesState>`.

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
A task-specific manager that encapsulates Room details and uses the `TaskEntityMapper` to bridge between flat entities and recursive domain trees.

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
