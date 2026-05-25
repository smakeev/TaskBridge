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
        print("[TaskBridge][iOSReminderHandler] Event emitter attached")
    }

    func getAllReminders() async throws -> [Reminder] {
        let requests = await notificationCenter.pendingNotificationRequests()
        let reminders = requests.compactMap(reminder(from:))
        print("[TaskBridge][iOSReminderHandler] getAllReminders count=\(reminders.count)")
        return reminders
    }

    func scheduleReminder(reminder: Reminder) async throws {
        print("[TaskBridge][iOSReminderHandler] scheduleReminder id=\(reminder.id.value) title=\(reminder.title)")

        try await ensureNotificationPermission()

        let content = UNMutableNotificationContent()
        content.title = reminder.title
        content.body = reminder.body
        content.sound = .default
        content.userInfo = dictionary(from: reminder)

        let date = Date(timeIntervalSince1970: Double(reminder.triggerAtMillis) / 1000.0)
        let components = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)

        let request = UNNotificationRequest(identifier: reminder.id.value, content: content, trigger: trigger)

        do {
            try await notificationCenter.add(request)
            print("[TaskBridge][iOSReminderHandler] notification scheduled id=\(reminder.id.value)")
        } catch {
            print("[TaskBridge][iOSReminderHandler] notification scheduling failed id=\(reminder.id.value) error=\(error)")
            throw error
        }

        try await emitUpdate()
    }

    func cancelReminder(reminderId: ReminderId) async throws {
        print("[TaskBridge][iOSReminderHandler] cancelReminder id=\(reminderId.value)")
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [reminderId.value])
        try await emitUpdate()
    }

    private func emitUpdate() async throws {
        let reminders = try await getAllReminders()
        print("[TaskBridge][iOSReminderHandler] emitUpdate count=\(reminders.count) emitterAttached=\(eventEmitter != nil)")
        try await eventEmitter?.remindersUpdated(reminders: reminders)
    }

    private func ensureNotificationPermission() async throws {
        let settings = await notificationCenter.notificationSettings()
        print("[TaskBridge][iOSReminderHandler] notification authorization status=\(settings.authorizationStatus.rawValue)")

        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            return
        case .notDetermined:
            let granted = try await notificationCenter.requestAuthorization(options: [.alert, .sound, .badge])
            print("[TaskBridge][iOSReminderHandler] notification permission requested granted=\(granted)")
            if !granted {
                throw NSError(
                    domain: "TaskBridge.iOSReminderHandler",
                    code: 1,
                    userInfo: [NSLocalizedDescriptionKey: "Notification permission was not granted."]
                )
            }
        case .denied:
            print("[TaskBridge][iOSReminderHandler] notification permission denied")
            throw NSError(
                domain: "TaskBridge.iOSReminderHandler",
                code: 2,
                userInfo: [NSLocalizedDescriptionKey: "Notification permission is denied."]
            )
        @unknown default:
            throw NSError(
                domain: "TaskBridge.iOSReminderHandler",
                code: 3,
                userInfo: [NSLocalizedDescriptionKey: "Unknown notification permission status."]
            )
        }
    }

    private func reminder(from request: UNNotificationRequest) -> Reminder? {
        let userInfo = request.content.userInfo
        let typeValue = userInfo["type"] as? String
        let triggerAtMillis = int64Value(userInfo["triggerAtMillis"]) ?? nextTriggerMillis(from: request.trigger)
        let createdAtMillis = int64Value(userInfo["createdAtMillis"]) ?? 0

        guard let triggerAtMillis else {
            print("[TaskBridge][iOSReminderHandler] skip request without trigger date id=\(request.identifier)")
            return nil
        }

        return Reminder(
            id: ReminderId(value: request.identifier),
            title: request.content.title,
            body: request.content.body,
            type: typeValue == "DEADLINE" ? .deadline : .start,
            triggerAtMillis: triggerAtMillis,
            createdAtMillis: createdAtMillis
        )
    }

    private func nextTriggerMillis(from trigger: UNNotificationTrigger?) -> Int64? {
        if let calendarTrigger = trigger as? UNCalendarNotificationTrigger,
           let date = calendarTrigger.nextTriggerDate() {
            return Int64(date.timeIntervalSince1970 * 1000)
        }
        return nil
    }

    private func dictionary(from reminder: Reminder) -> [String: Any] {
        [
            "id": reminder.id.value,
            "title": reminder.title,
            "body": reminder.body,
            "type": reminder.type == .deadline ? "DEADLINE" : "START",
            "triggerAtMillis": reminder.triggerAtMillis,
            "createdAtMillis": reminder.createdAtMillis
        ]
    }

    private func int64Value(_ value: Any?) -> Int64? {
        if let value = value as? Int64 {
            return value
        }
        if let value = value as? Int {
            return Int64(value)
        }
        if let value = value as? Double {
            return Int64(value)
        }
        if let value = value as? NSNumber {
            return value.int64Value
        }
        return nil
    }
}
