import SwiftUI
import TaskBridgeCore

struct TasksNavigationView: View {
    @Environment(\.navigationRepository) private var navigationRepository
    @State private var currentDestination: NavigationDestination?

    var body: some View {
        DestinationMapper(destination: currentDestination)
            .task {
                await observeNavigation(rootType: NavigationDestinationTasksRoot.self)
            }
    }

    private func observeNavigation(rootType: Any.Type) async {
        guard let navigationRepository else { return }
        for await path in navigationRepository.activePath {
            if let path, let root = path.root, type(of: root) == rootType {
                currentDestination = path.current
            }
        }
    }
}

struct TemplatesNavigationView: View {
    @Environment(\.navigationRepository) private var navigationRepository
    @State private var currentDestination: NavigationDestination?

    var body: some View {
        DestinationMapper(destination: currentDestination)
            .task {
                await observeNavigation(rootType: NavigationDestinationTemplatesRoot.self)
            }
    }

    private func observeNavigation(rootType: Any.Type) async {
        guard let navigationRepository else { return }
        for await path in navigationRepository.activePath {
            if let path, let root = path.root, type(of: root) == rootType {
                currentDestination = path.current
            }
        }
    }
}

struct RemindersNavigationView: View {
    @Environment(\.navigationRepository) private var navigationRepository
    @State private var currentDestination: NavigationDestination?

    var body: some View {
        DestinationMapper(destination: currentDestination)
            .task {
                await observeNavigation(rootType: NavigationDestinationRemindersRoot.self)
            }
    }

    private func observeNavigation(rootType: Any.Type) async {
        guard let navigationRepository else { return }
        for await path in navigationRepository.activePath {
            if let path, let root = path.root, type(of: root) == rootType {
                currentDestination = path.current
            }
        }
    }
}

struct DestinationMapper: View {
    let destination: NavigationDestination?

    var body: some View {
        Group {
            if let destination {
                switch destination {
                case is NavigationDestinationTasksRoot:
                    TasksRootView()
                case is NavigationDestinationTemplatesRoot:
                    TemplatesRootView()
                case is NavigationDestinationRemindersRoot:
                    RemindersRootView()
                case let details as NavigationDestinationTaskDetails:
                    TaskDetailsView(taskId: details.taskId)
                case let create as NavigationDestinationCreateTask:
                    CreateTaskView(parentTaskId: create.parentTaskId)
                case let input as NavigationDestinationTemplateNameInput:
                    TemplateNameInputView(templateId: input.templateId)
                case let details as NavigationDestinationReminderDetails:
                    ReminderDetailsView(reminderId: details.reminderId)
                default:
                    Text("Unknown Destination")
                }
            } else {
                ProgressView()
            }
        }
    }
}
