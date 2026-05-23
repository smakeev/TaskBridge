# TaskBridge Core Entities

This document provides a comprehensive description of all entities defined within the TaskBridge Core module.

## Core Logic
### TaskBridge
The main entry point for the Core logic.
- `message`: A sample string property used for initial verification of the KMP setup.

## Services
### Service
A base interface for all application services.

### BaseStatefulService
An abstract base class for services that maintain state. It implements the "Actor" pattern using a `Channel` and a `launch` loop to ensure that all state updates are processed sequentially and thread-safely. It uses a `suspend` handleCommand function to allow for asynchronous state transitions.

### ServiceCommand
A marker interface for commands that can be processed by services.

### AppStateCommand
A sealed interface representing commands for the `AppStateService`.
- `SelectTab(tab: AppTab)`: Command to switch the currently selected application tab.

### ServiceData
A marker interface for data structures used by services.

### StatefulService
A generic interface for services that maintain a state (`ServiceData`) and process commands (`ServiceCommand`). It exposes the state via a `StateFlow`.

### AppStateServiceData
A data class containing the global application state, primarily the navigation state. It provides a default initial state where all tabs are initialized to their respective roots.

### AppStateService (Internal)
An internal implementation of `BaseStatefulService` that manages the `AppStateServiceData`.

---

## Navigation
### AppTab
An enum representing the primary navigation tabs in the application.
- `TASKS`: The main task list view.
- `TEMPLATES`: View for managing task templates.
- `REMINDERS`: View for managing task reminders.

### NavigationDestination
A sealed interface representing specific screens or views within a tab's navigation stack.
- `TasksRoot`: The root view of the Tasks tab.
- `TemplatesRoot`: The root view of the Templates tab.
- `RemindersRoot`: The root view of the Reminders tab.
- `TaskDetails(taskId: String)`: Detailed view of a specific task.
- `CreateTask(parentTaskId: String?)`: View for creating a new task.
- `TemplateNameInput(templateId: String)`: Input view for template naming/editing.
- `ReminderDetails(reminderId: String)`: Detailed view of a specific reminder.

### NavigationPath
A data class representing a stack of `NavigationDestination` objects.
- `destinations`: A list of destinations forming the current stack.
- `root`: Returns the first destination in the stack.
- `current`: Returns the top destination in the stack.
- `canGoBack`: Boolean indicating if there is more than one destination in the stack.

### NavigationOverlay
A sealed interface representing temporary UI elements shown over the main navigation state.
- `Alert`: A modal dialog with a title, message, and actions.
- `Sheet`: A modal sheet containing its own navigation path of `OverlayDestination`.
- `BottomOverlay`: A bottom-anchored overlay with a specific height percentage and its own navigation path.

#### OverlayDestination
Destinations specific to overlay stacks.
- `SelectTaskType(parentTaskId: String?)`: View to select the type of task to create.

#### OverlayAction & OverlayActionStyle
Defines actions for alerts.
- `OverlayAction`: Contains an ID, title key (for localization), and style.
- `OverlayActionStyle`: `DEFAULT`, `CANCEL`, or `DESTRUCTIVE`.

### NavigationState
The root state object for application navigation.
- `selectedTab`: The currently active `AppTab`.
- `paths`: A map of navigation stacks for each tab.
- `overlay`: The currently active `NavigationOverlay`, if any.
- `activePath`: Helper property to retrieve the path for the currently selected tab.
