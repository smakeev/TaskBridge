import Foundation
import TaskBridgeCore

@MainActor
final class TasksViewModel: ObservableObject {
    @Published var state = TasksState(tasks: [], isLoading: false, errorMessage: nil)
    
    private let repository: TasksRepository
    private var observationTask: Task<Void, Never>?
    private var hasLoaded = false
    
    init(repository: TasksRepository) {
        self.repository = repository
        observationTask = Task {
            for await newState in repository.tasksState {
                self.state = newState
            }
        }
    }
    
    deinit {
        observationTask?.cancel()
    }
    
    func loadTasks() {
        guard !hasLoaded else { return }
        hasLoaded = true
        Task {
            try? await repository.loadTasks()
        }
    }
    
    func createTask(title: String, type: TaskType, parentId: TaskId? = nil) {
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedTitle.isEmpty else { return }

        Task {
            try? await repository.createTask(newTask(title: trimmedTitle, type: type, parentId: parentId))
        }
    }

    func createSubtask(parentTask: TaskItem, title: String, type: TaskType) {
        createTask(title: title, type: type, parentId: parentTask.id)
    }

    func deleteTaskTree(taskId: TaskId) {
        Task {
            try? await repository.deleteTaskTree(taskId)
        }
    }
    
    func toggleCheckbox(task: TaskItem) {
        guard task.type == .checkbox else { return }
        let updatedTask = TaskItem(
            id: task.id,
            parentId: task.parentId,
            title: task.title,
            type: task.type,
            isDone: KotlinBoolean(bool: !(task.isChecked)),
            progress: task.progress,
            children: task.children
        )
        Task {
            try? await repository.replaceTask(updatedTask)
        }
    }
    
    func updateProgress(task: TaskItem, progress: Int) {
        guard task.type == .progress else { return }
        let clampedProgress = min(max(progress, 0), 100)
        let updatedTask = TaskItemInterop.shared.withProgress(
            task: task,
            progressValue: Int32(clampedProgress)
        )
        Task {
            try? await repository.replaceTask(updatedTask)
        }
    }
    
    func renameTask(task: TaskItem, newTitle: String) {
        let trimmedTitle = newTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedTitle.isEmpty else { return }
        
        let updatedTask = TaskItem(
            id: task.id,
            parentId: task.parentId,
            title: trimmedTitle,
            type: task.type,
            isDone: task.isDone,
            progress: task.progress,
            children: task.children
        )
        Task {
            try? await repository.replaceTask(updatedTask)
        }
    }
    
    private func newTask(title: String, type: TaskType, parentId: TaskId?) -> TaskItem {
        let taskId = TaskId(value: UUID().uuidString)
        switch type {
        case .checkbox:
            return TaskItem(
                id: taskId,
                parentId: parentId,
                title: title,
                type: type,
                isDone: KotlinBoolean(bool: false),
                progress: nil,
                children: []
            )
        case .progress:
            return TaskItemInterop.shared.progressTask(
                id: taskId,
                parentId: parentId,
                title: title
            )
        case .container:
            return TaskItem(
                id: taskId,
                parentId: parentId,
                title: title,
                type: type,
                isDone: nil,
                progress: nil,
                children: []
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

extension TaskItem {
    var taskIdValue: String {
        id.value
    }

    var isChecked: Bool {
        isDone?.boolValue == true
    }

    var progressValue: Int {
        Int(TaskItemInterop.shared.progressValue(task: self))
    }
}
