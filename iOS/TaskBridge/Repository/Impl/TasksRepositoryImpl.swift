import Foundation
import TaskBridgeCore

@MainActor
class TasksRepositoryImpl: TasksRepository {
    private let interactor: TasksInteractor
    
    init(interactor: TasksInteractor) {
        self.interactor = interactor
    }
    
    var tasksState: AsyncStream<TasksState> {
        return createStream(for: interactor.tasksState)
    }
    
    func loadTasks() async throws {
        try await interactor.loadTasks()
    }
    
    func createTask(_ task: TaskItem) async throws {
        try await interactor.createTask(task: task)
    }
    
    func replaceTask(_ task: TaskItem) async throws {
        try await interactor.replaceTask(task: task)
    }
    
    func deleteTaskTree(_ taskId: TaskId) async throws {
        try await interactor.deleteTaskTree(taskId: taskId)
    }
}
