import Foundation
import TaskBridgeCore

@MainActor
protocol RemindersRepository {
    var remindersState: AsyncStream<RemindersState> { get }
    
    func scheduleReminder(_ reminder: Reminder) async throws
    func cancelReminder(_ reminderId: ReminderId) async throws
}
