import Foundation
import TaskBridgeCore

@MainActor
final class RemindersViewModel: ObservableObject {
    @Published var state = RemindersState(reminders: [], isLoading: false, errorMessage: nil)
    
    private let repository: RemindersRepository
    private var observationTask: Task<Void, Never>?
    private var hasLoaded = false
    
    init(repository: RemindersRepository) {
        self.repository = repository
        observationTask = Task {
            for await newState in repository.remindersState {
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
            try? await repository.loadReminders()
        }
    }
    
    func cancelReminder(_ reminder: Reminder) {
        Task {
            try? await repository.cancelReminder(reminder.id)
        }
    }
}

extension Reminder {
    var reminderIdValue: String {
        id.value
    }
}
