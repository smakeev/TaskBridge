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
    private let remindersRepository: RemindersRepository
    
    init(
        navigationRepository: NavigationRepository,
        tasksRepository: TasksRepository,
        remindersRepository: RemindersRepository
    ) {
        self.navigationRepository = navigationRepository
        self.tasksRepository = tasksRepository
        self.remindersRepository = remindersRepository
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
            tasksRepository: tasksRepository,
            remindersRepository: remindersRepository
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
            tasksRepository: tasksRepository,
            remindersRepository: nil
        )
    }
}

struct RemindersNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    private let navigationRepository: NavigationRepository
    private let remindersRepository: RemindersRepository
    
    init(navigationRepository: NavigationRepository, remindersRepository: RemindersRepository) {
        self.navigationRepository = navigationRepository
        self.remindersRepository = remindersRepository
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: navigationRepository,
            rootType: NavigationDestinationRemindersRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(
            destination: viewModel.currentDestination,
            navigationRepository: navigationRepository,
            templatesRepository: nil,
            tasksRepository: nil,
            remindersRepository: remindersRepository
        )
    }
}

struct DestinationMapper: View {
    let destination: NavigationDestination?
    let navigationRepository: NavigationRepository
    let templatesRepository: TaskTemplatesRepository?
    let tasksRepository: TasksRepository?
    let remindersRepository: RemindersRepository?
    
    var body: some View {
        Group {
            if let destination = destination {
                switch destination {
                case is NavigationDestinationTasksRoot:
                    if let tasksRepository, let remindersRepository {
                        TasksRootView(
                            tasksRepository: tasksRepository,
                            remindersRepository: remindersRepository,
                            navigationRepository: navigationRepository
                        )
                    }
                case is NavigationDestinationTemplatesRoot: 
                    if let repository = templatesRepository, let tasksRepository {
                        TemplatesRootView(
                            repository: repository,
                            tasksRepository: tasksRepository,
                            navigationRepository: navigationRepository
                        )
                    }
                case is NavigationDestinationRemindersRoot:
                    if let remindersRepository {
                        RemindersRootView(repository: remindersRepository)
                    }
                case let details as NavigationDestinationTaskDetails:
                    if let tasksRepository, let remindersRepository {
                        TaskDetailsView(
                            taskId: details.taskId,
                            tasksRepository: tasksRepository,
                            remindersRepository: remindersRepository,
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
