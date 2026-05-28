import Foundation
import TaskBridgeCore

@MainActor
protocol RemindersRepository: AnyObject {
    var remindersState: AsyncStream<RemindersState> { get }
    
    func loadReminders() async throws
    func scheduleReminder(_ reminder: Reminder) async throws
    func cancelReminder(_ reminderId: ReminderId) async throws
}
