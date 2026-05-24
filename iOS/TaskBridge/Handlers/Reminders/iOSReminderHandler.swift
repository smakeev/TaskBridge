import Foundation
import UserNotifications
import TaskBridgeCore

/**
 * iOS implementation of ReminderHandler.
 * Uses UNUserNotificationCenter to schedule notifications and reconstructs Reminder models
 * from actual pending notification requests.
 */
class iOSReminderHandler: NSObject, ReminderHandler {
    private var eventEmitter: CoreEventEmitter? = null
    private val notificationCenter = UNUserNotificationCenter.current()
    
    func setEventEmitter(emitter: CoreEventEmitter) {
        self.eventEmitter = emitter
    }
    
    func getAllReminders() async throws -> [Reminder] {
        let requests = await notificationCenter.pendingNotificationRequests()
        return requests.compactMap { request in
            // Reconstruct Reminder from UNNotificationRequest
            // We use the identifier as the ID. 
            // In production, additional metadata could be stored in userInfo.
            
            let triggerAtMillis: Int64
            if let calendarTrigger = request.trigger as? UNCalendarNotificationTrigger,
               let date = calendarTrigger.nextTriggerDate() {
                triggerAtMillis = Int64(date.timeIntervalSince1970 * 1000)
            } else {
                triggerAtMillis = 0
            }
            
            return Reminder(
                id: ReminderId(value: request.identifier),
                title: request.content.title,
                body: request.content.body,
                type: .start, // Assuming START for pending ones for now
                triggerAtMillis: triggerAtMillis,
                createdAtMillis: 0 // Not stored in pending request by default
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
