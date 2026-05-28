import Foundation
import TaskBridgeCore

@MainActor
final class RemindersViewModel: ObservableObject {
    @Published var state = RemindersState(reminders: [], isLoading: false, errorMessage: nil)

    private let repository: RemindersRepository
    private let navigationRepository: NavigationRepository
    private let messagesRepository: MessagesRepository
    private var observationTask: Task<Void, Never>?
    private var hasLoaded = false

    init(
        repository: RemindersRepository,
        navigationRepository: NavigationRepository,
        messagesRepository: MessagesRepository
    ) {
        self.repository = repository
        self.navigationRepository = navigationRepository
        self.messagesRepository = messagesRepository
        observationTask = Task { [weak self, repository] in
            for await newState in repository.remindersState {
                guard let self else { break }
                self.state = newState
            }
        }
    }

    deinit {
        observationTask?.cancel()
    }

    func loadReminders() {
        guard !hasLoaded else { return }
        hasLoaded = true
        Task {
            do {
                try await repository.loadReminders()
                print("[TaskBridge][iOS][RemindersViewModel] load requested")
            } catch {
                print("[TaskBridge][iOS][RemindersViewModel] load failed error=\(error)")
            }
        }
    }

    func cancelReminder(_ reminder: Reminder) {
        Task {
            do {
                try await repository.cancelReminder(reminder.id)
                print("[TaskBridge][iOS][RemindersViewModel] cancel requested id=\(reminder.id.value)")
            } catch {
                print("[TaskBridge][iOS][RemindersViewModel] cancel failed error=\(error)")
            }
        }
    }

    func consumeNavigationDestinationMessage(scopeId: String) async throws -> NavigationDestinationMessage? {
        try await navigationRepository.consumeNavigationDestinationMessage(scopeId: scopeId)
    }

    func observeReminderCreatedMessages() -> AsyncStream<AppMessage> {
        messagesRepository.observe(type: AppMessageKeys.shared.reminderCreated)
    }
}

extension Reminder {
    var reminderIdValue: String {
        id.value
    }
}
