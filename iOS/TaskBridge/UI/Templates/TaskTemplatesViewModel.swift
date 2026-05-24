import SwiftUI
import TaskBridgeCore

@MainActor
class TaskTemplatesViewModel: ObservableObject {
    @Published var state: TaskTemplatesState = TaskTemplatesState(templates: [], isLoading: false, errorMessage: nil, lastLoadedAtMillis: nil)
    private let repository: TaskTemplatesRepository
    private let tasksRepository: TasksRepository
    private let navigationRepository: NavigationRepository
    private var observationTask: Task<Void, Never>?
    
    init(
        repository: TaskTemplatesRepository,
        tasksRepository: TasksRepository,
        navigationRepository: NavigationRepository
    ) {
        self.repository = repository
        self.tasksRepository = tasksRepository
        self.navigationRepository = navigationRepository
        
        observationTask = Task {
            for await newState in repository.templatesState {
                self.state = newState
            }
        }
    }
    
    deinit {
        observationTask?.cancel()
    }
    
    func loadTemplates() {
        Task {
            try? await repository.loadTemplates()
        }
    }
    
    func refreshTemplates() async {
        try? await repository.loadTemplates()
    }

    func addTask(from template: TaskTemplate, title: String) {
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedTitle.isEmpty else { return }

        Task {
            try? await tasksRepository.createTask(
                makeTask(from: template.rootTask, parentId: nil, titleOverride: trimmedTitle)
            )
            try? await navigationRepository.pullToRoot(tab: .tasks)
            try? await navigationRepository.selectTab(tab: .tasks)
        }
    }

    private func makeTask(from templateTask: TemplateTaskItem, parentId: TaskId?, titleOverride: String? = nil) -> TaskItem {
        let taskId = TaskId(value: UUID().uuidString)
        let title = titleOverride ?? templateTask.title

        switch templateTask.type {
        case .checkbox:
            return TaskItem(
                id: taskId,
                parentId: parentId,
                title: title,
                type: .checkbox,
                isDone: KotlinBoolean(bool: false),
                progress: nil,
                children: []
            )
        case .progress:
            let initialProgress = Int(templateTask.initialProgress?.intValue ?? 0)
            return TaskItemInterop.shared.withProgress(
                task: TaskItemInterop.shared.progressTask(
                    id: taskId,
                    parentId: parentId,
                    title: title
                ),
                progressValue: Int32(initialProgress)
            )
        case .container:
            let children = (templateTask.children ?? []).map {
                makeTask(from: $0, parentId: taskId)
            }
            return TaskItem(
                id: taskId,
                parentId: parentId,
                title: title,
                type: .container,
                isDone: nil,
                progress: nil,
                children: children
            )
        default:
            return TaskItem(
                id: taskId,
                parentId: parentId,
                title: title,
                type: .checkbox,
                isDone: KotlinBoolean(bool: false),
                progress: nil,
                children: []
            )
        }
    }
}
