import SwiftUI
import TaskBridgeCore

struct TasksNavigationView: View {
    var body: some View {
        NavigationScreen(rootType: NavigationDestinationTasksRoot.self)
    }
}

struct TemplatesNavigationView: View {
    var body: some View {
        NavigationScreen(rootType: NavigationDestinationTemplatesRoot.self)
    }
}

struct RemindersNavigationView: View {
    var body: some View {
        NavigationScreen(rootType: NavigationDestinationRemindersRoot.self)
    }
}

private struct NavigationScreen: View {
    let rootType: Any.Type

    @Environment(\.repositoriesStorage) private var repositoriesStorage
    @State private var navigationRepository: NavigationRepository?
    @State private var currentDestination: NavigationDestination?

    var body: some View {
        DestinationMapper(destination: currentDestination)
            .task {
                await observeNavigation()
            }
    }

    private func observeNavigation() async {
        let repository = navigationRepository ?? repositoriesStorage?.navigationRepository
        navigationRepository = repository
        guard let navigationRepository = repository else { return }
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
