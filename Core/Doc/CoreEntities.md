# TaskBridge Core Entities

This document describes the entities in the TaskBridge Core module.

## Core Entry Point
### TaskBridge
The main entry point for the Core module. It provides access to public Interactors (e.g., `navigationInteractor()`). Internal Use Case access is hidden from the platform.

---

## Interactors (Platform Layer)
High-level bridges used by the platform to interact with the Core.

### NavigationInteractor
Public class providing a simplified interface for navigation logic. Assembled by the `CoreAssembler`.
- `activePath`: `Flow<NavigationPath?>`
- `currentTab`: `Flow<AppTab>`
- `overlay`: `Flow<NavigationOverlay?>`
- `selectTab(tab: AppTab)`: Selects the specified tab.
- `fetchActivePath()`: Suspends and returns the current path.
- `fetchCurrentTab()`: Suspends and returns the current tab.
- `fetchOverlay()`: Suspends and returns the current overlay.

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
