# TaskBridge Core Entities

This document describes the entities in the TaskBridge Core module.

## Core Entry Point
### TaskBridge
The main entry point for the Core module. It provides access to public Use Cases via a generic `getUseCase(type: KClass<T>)` method.

---

## Interactors (Platform Layer)
High-level bridges used by the platform to interact with the Core.

### NavigationInteractor
Public class providing a simplified interface for navigation logic.
- `activePath`: `Flow<NavigationPath?>`
- `currentTab`: `Flow<AppTab>`
- `selectTab(tab: AppTab)`: Selects the specified tab.
- `fetchActivePath()`: Suspends and returns the current path.
- `fetchCurrentTab()`: Suspends and returns the current tab.

---

## Composition
Internal components for dependency management.

### CoreAssembler (Internal)
The composition root that holds the service locator and containers.

### CoreServiceLocator (Internal)
Manages the lifecycle of internal services. Lazily creates and keeps service instances.

### UserStoriesContainer (Internal)
Creates internal User Stories via explicit getters.

### UseCaseContainer (Internal)
Creates public Use Cases. Provides access by type via a generic `get` method.

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

---

## Use Cases (Public)
### SelectTabUseCase
Public API to trigger tab selection.
- `selectTab(tab: AppTab)`: Selects the specified application tab.

### NavigationStateObserverUseCase
Public API to observe or fetch navigation-related state.
- `subscribeOnActivePath()`: Returns a `Flow<NavigationPath?>`.
- `subscribeOnCurrentTab()`: Returns a `Flow<AppTab>`.
- `fetchActivePath()`: Suspends until the first `NavigationPath?` is available.
- `fetchCurrentTab()`: Suspends until the first `AppTab` is available.

---

## Services
### Service
Base interface for all services.

### BaseStatefulService
Abstract base class for stateful services using the Actor pattern.

### StatefulService
Interface for services with reactive state and command processing.

### AppStateService (Internal)
Manages the application state, including navigation.
