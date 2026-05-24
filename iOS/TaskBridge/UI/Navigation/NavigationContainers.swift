import SwiftUI
import TaskBridgeCore

@MainActor
class NavigationViewModel: ObservableObject {
    @Published var currentDestination: NavigationDestination?
    private let repository: NavigationRepository
    private let rootType: Any.Type
    private var observationTask: Task<Void, Never>?
    
    init(repository: NavigationRepository, rootType: Any.Type) {
        self.repository = repository
        self.rootType = rootType
        
        observationTask = Task {
            for await path in repository.activePath {
                // Only update if the path belongs to this tab's root
                if let path = path, let root = path.root, type(of: root) == rootType {
                    self.currentDestination = path.current
                }
                // Else: ignore foreign paths to preserve last valid destination
            }
        }
    }
    
    deinit {
        observationTask?.cancel()
    }
}

struct TasksNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    private let navigationRepository: NavigationRepository
    private let tasksRepository: TasksRepository
    
    init(navigationRepository: NavigationRepository, tasksRepository: TasksRepository) {
        self.navigationRepository = navigationRepository
        self.tasksRepository = tasksRepository
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: navigationRepository,
            rootType: NavigationDestinationTasksRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(
            destination: viewModel.currentDestination,
            navigationRepository: navigationRepository,
            templatesRepository: nil,
            tasksRepository: tasksRepository
        )
    }
}

struct TemplatesNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    private let navigationRepository: NavigationRepository
    private let templatesRepository: TaskTemplatesRepository
    private let tasksRepository: TasksRepository
    
    init(
        navigationRepository: NavigationRepository,
        templatesRepository: TaskTemplatesRepository,
        tasksRepository: TasksRepository
    ) {
        self.navigationRepository = navigationRepository
        self.templatesRepository = templatesRepository
        self.tasksRepository = tasksRepository
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: navigationRepository,
            rootType: NavigationDestinationTemplatesRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(
            destination: viewModel.currentDestination,
            navigationRepository: navigationRepository,
            templatesRepository: templatesRepository,
            tasksRepository: tasksRepository
        )
    }
}

struct RemindersNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    private let navigationRepository: NavigationRepository
    
    init(repository: NavigationRepository) {
        self.navigationRepository = repository
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: repository,
            rootType: NavigationDestinationRemindersRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(
            destination: viewModel.currentDestination,
            navigationRepository: navigationRepository,
            templatesRepository: nil,
            tasksRepository: nil
        )
    }
}

struct DestinationMapper: View {
    let destination: NavigationDestination?
    let navigationRepository: NavigationRepository
    let templatesRepository: TaskTemplatesRepository?
    let tasksRepository: TasksRepository?
    
    var body: some View {
        Group {
            if let destination = destination {
                switch destination {
                case is NavigationDestinationTasksRoot:
                    if let tasksRepository {
                        TasksRootView(tasksRepository: tasksRepository, navigationRepository: navigationRepository)
                    }
                case is NavigationDestinationTemplatesRoot: 
                    if let repository = templatesRepository, let tasksRepository {
                        TemplatesRootView(
                            repository: repository,
                            tasksRepository: tasksRepository,
                            navigationRepository: navigationRepository
                        )
                    }
                case is NavigationDestinationRemindersRoot: RemindersRootView()
                case let details as NavigationDestinationTaskDetails:
                    if let tasksRepository {
                        TaskDetailsView(
                            taskId: details.taskId,
                            tasksRepository: tasksRepository,
                            navigationRepository: navigationRepository
                        )
                    }
                case let create as NavigationDestinationCreateTask: CreateTaskView(parentTaskId: create.parentTaskId)
                case let input as NavigationDestinationTemplateNameInput: TemplateNameInputView(templateId: input.templateId)
                case let details as NavigationDestinationReminderDetails: ReminderDetailsView(reminderId: details.reminderId)
                default: Text("Unknown Destination")
                }
            } else {
                ProgressView()
            }
        }
    }
}
