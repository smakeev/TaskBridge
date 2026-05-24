import Foundation
import UserNotifications
import TaskBridgeCore

/**
 * iOS implementation of ReminderHandler.
 * Uses UNUserNotificationCenter to schedule notifications and reconstructs Reminder models
 * from actual pending notification requests.
 */
class iOSReminderHandler: NSObject, ReminderHandler {
    private var eventEmitter: CoreEventEmitter? = nil
    private let notificationCenter = UNUserNotificationCenter.current()
    
    func setEventEmitter(emitter: CoreEventEmitter) {
        self.eventEmitter = emitter
    }
    
    func getAllReminders() async throws -> [Reminder] {
        let requests = await notificationCenter.pendingNotificationRequests()
        return requests.compactMap { request in
            // Reconstruct Reminder from UNNotificationRequest
            
            var triggerAtMillis: Int64 = 0
            if let calendarTrigger = request.trigger as? UNCalendarNotificationTrigger,
               let date = calendarTrigger.nextTriggerDate() {
                triggerAtMillis = Int64(date.timeIntervalSince1970 * 1000)
            }
            
            return Reminder(
                id: ReminderId(value: request.identifier),
                title: request.content.title,
                body: request.content.body,
                type: .start,
                triggerAtMillis: triggerAtMillis,
                createdAtMillis: 0
            )
        }
    }
    
    func scheduleReminder(reminder: Reminder) async throws {
        let content = UNMutableNotificationContent()
        content.title = reminder.title
        content.body = reminder.body
        content.sound = .default
        
        let date = Date(timeIntervalSince1970: Double(reminder.triggerAtMillis) / 1000.0)
        let components = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        
        let request = UNNotificationRequest(identifier: reminder.id.value, content: content, trigger: trigger)
        
        try await notificationCenter.add(request)
        try await emitUpdate()
    }
    
    func cancelReminder(reminderId: ReminderId) async throws {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [reminderId.value])
        try await emitUpdate()
    }
    
    private func emitUpdate() async throws {
        let reminders = try await getAllReminders()
        try await eventEmitter?.remindersUpdated(reminders: reminders)
    }
}
