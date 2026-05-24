import Foundation
import TaskBridgeCore

@MainActor
protocol TasksRepository {
    var tasksState: AsyncStream<TasksState> { get }
    
    func loadTasks() async throws
    func createTask(_ task: TaskItem) async throws
    func replaceTask(_ task: TaskItem) async throws
    func deleteTaskTree(_ taskId: TaskId) async throws
}
