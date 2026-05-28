import SwiftUI
import TaskBridgeCore

@MainActor
final class RepositoriesStorage {
    private let taskBridge: TaskBridge

    private weak var navigationRepositoryRef: NavigationRepository?
    private weak var taskTemplatesRepositoryRef: TaskTemplatesRepository?
    private weak var tasksRepositoryRef: TasksRepository?
    private weak var remindersRepositoryRef: RemindersRepository?
    private weak var messagesRepositoryRef: MessagesRepository?

    init() {
        let platformDependencies = PlatformDependencies()
        let platformHandlers = CorePlatformHandlers(
            reminderHandler: iOSReminderHandler()
        )
        self.taskBridge = TaskBridge(platformDependencies: platformDependencies, platformHandlers: platformHandlers)
    }

    var navigationRepository: NavigationRepository {
        if let repository = navigationRepositoryRef {
            return repository
        }
        let repository = NavigationRepositoryImpl(interactor: taskBridge.navigationInteractor())
        navigationRepositoryRef = repository
        return repository
    }

    var taskTemplatesRepository: TaskTemplatesRepository {
        if let repository = taskTemplatesRepositoryRef {
            return repository
        }
        let repository = TaskTemplatesRepositoryImpl(interactor: taskBridge.templatesInteractor())
        taskTemplatesRepositoryRef = repository
        return repository
    }

    var tasksRepository: TasksRepository {
        if let repository = tasksRepositoryRef {
            return repository
        }
        let repository = TasksRepositoryImpl(interactor: taskBridge.tasksInteractor())
        tasksRepositoryRef = repository
        return repository
    }

    var remindersRepository: RemindersRepository {
        if let repository = remindersRepositoryRef {
            return repository
        }
        let repository = RemindersRepositoryImpl(interactor: taskBridge.remindersInteractor())
        remindersRepositoryRef = repository
        return repository
    }

    var messagesRepository: MessagesRepository {
        if let repository = messagesRepositoryRef {
            return repository
        }
        let repository = MessagesRepositoryImpl(interactor: taskBridge.messagesInteractor())
        messagesRepositoryRef = repository
        return repository
    }
}

struct RepositoriesStorageKey: EnvironmentKey {
    @MainActor static var defaultValue: RepositoriesStorage? = nil
}

extension EnvironmentValues {
    var repositoriesStorage: RepositoriesStorage? {
        get { self[RepositoriesStorageKey.self] }
        set { self[RepositoriesStorageKey.self] = newValue }
    }
}

@main
struct TaskBridgeApp: App {
    private let repositoriesStorage: RepositoriesStorage

    init() {
        self.repositoriesStorage = RepositoriesStorage()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.repositoriesStorage, repositoriesStorage)
        }
    }
}
