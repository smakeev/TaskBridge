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
    
    init(repository: NavigationRepository) {
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: repository,
            rootType: NavigationDestinationTasksRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(destination: viewModel.currentDestination, templatesRepository: nil)
    }
}

struct TemplatesNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    private let templatesRepository: TaskTemplatesRepository
    
    init(navigationRepository: NavigationRepository, templatesRepository: TaskTemplatesRepository) {
        self.templatesRepository = templatesRepository
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: navigationRepository,
            rootType: NavigationDestinationTemplatesRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(destination: viewModel.currentDestination, templatesRepository: templatesRepository)
    }
}

struct RemindersNavigationView: View {
    @StateObject private var viewModel: NavigationViewModel
    
    init(repository: NavigationRepository) {
        _viewModel = StateObject(wrappedValue: NavigationViewModel(
            repository: repository,
            rootType: NavigationDestinationRemindersRoot.self
        ))
    }
    
    var body: some View {
        DestinationMapper(destination: viewModel.currentDestination, templatesRepository: nil)
    }
}

struct DestinationMapper: View {
    let destination: NavigationDestination?
    let templatesRepository: TaskTemplatesRepository?
    
    var body: some View {
        Group {
            if let destination = destination {
                switch destination {
                case is NavigationDestinationTasksRoot: TasksRootView()
                case is NavigationDestinationTemplatesRoot: 
                    if let repository = templatesRepository {
                        TemplatesRootView(repository: repository)
                    }
                case is NavigationDestinationRemindersRoot: RemindersRootView()
                case let details as NavigationDestinationTaskDetails: TaskDetailsView(taskId: details.taskId)
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
