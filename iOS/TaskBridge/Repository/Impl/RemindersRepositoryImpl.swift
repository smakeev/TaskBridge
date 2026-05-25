import Foundation
import TaskBridgeCore

@MainActor
class RemindersRepositoryImpl: RemindersRepository {
    private let interactor: RemindersInteractor

    init(interactor: RemindersInteractor) {
        self.interactor = interactor
    }

    var remindersState: AsyncStream<RemindersState> {
        return createStream(for: interactor.remindersState)
    }

    func loadReminders() async throws {
        try await interactor.loadReminders()
    }

    func scheduleReminder(_ reminder: Reminder) async throws {
        try await interactor.scheduleReminder(reminder: reminder)
    }

    func cancelReminder(_ reminderId: ReminderId) async throws {
        try await interactor.cancelReminder(reminderId: reminderId)
    }
}
