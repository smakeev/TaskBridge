# TaskBridge Core Entities

This document describes the entities in the TaskBridge Core module.

## Core Entry Point
### TaskBridge
The main entry point for the Core module. It provides access to public Use Cases via a generic `getUseCase(type: KClass<T>)` method.

---

## Composition
Internal components for dependency management.

### CoreAssembler (Internal)
The composition root that holds the service locator and containers.

### CoreServiceLocator (Internal)
Manages the lifecycle of internal services. Lazily creates and keeps service instances.

### UserStoriesContainer (Internal)
Creates internal User Stories via explicit getters (e.g., `selectTab(assembler)`, `getAppStateService(assembler)`).

### UseCaseContainer (Internal)
Creates public Use Cases. Provides access by type via a generic `get` method.

---

## User Stories (Internal)
### GetAppStateServiceStory
A story that retrieves the `AppStateService` from the service locator.

### SelectTabStory
A story that orchestrates the `AppStateService` (obtained via `GetAppStateServiceStory`) to update the active tab.

---

## Use Cases (Public)
### SelectTabUseCase
Public API to trigger tab selection.

---

## Services
### Service
Base interface for all services.

### BaseStatefulService
Abstract base class for stateful services. Implements the Actor pattern for sequential state updates.

### StatefulService
Interface for services with state and commands.

### AppStateService (Internal)
Manages the application state, including navigation.
