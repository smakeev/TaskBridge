import SwiftUI
import TaskBridgeCore

struct NavigationRepositoryKey: EnvironmentKey {
    @MainActor static var defaultValue: NavigationRepository? = nil
}

struct TaskTemplatesRepositoryKey: EnvironmentKey {
    @MainActor static var defaultValue: TaskTemplatesRepository? = nil
}

struct TasksRepositoryKey: EnvironmentKey {
    @MainActor static var defaultValue: TasksRepository? = nil
}

struct RemindersRepositoryKey: EnvironmentKey {
    @MainActor static var defaultValue: RemindersRepository? = nil
}

struct MessagesRepositoryKey: EnvironmentKey {
    @MainActor static var defaultValue: MessagesRepository? = nil
}

extension EnvironmentValues {
    var navigationRepository: NavigationRepository? {
        get { self[NavigationRepositoryKey.self] }
        set { self[NavigationRepositoryKey.self] = newValue }
    }

    var taskTemplatesRepository: TaskTemplatesRepository? {
        get { self[TaskTemplatesRepositoryKey.self] }
        set { self[TaskTemplatesRepositoryKey.self] = newValue }
    }

    var tasksRepository: TasksRepository? {
        get { self[TasksRepositoryKey.self] }
        set { self[TasksRepositoryKey.self] = newValue }
    }

    var remindersRepository: RemindersRepository? {
        get { self[RemindersRepositoryKey.self] }
        set { self[RemindersRepositoryKey.self] = newValue }
    }

    var messagesRepository: MessagesRepository? {
        get { self[MessagesRepositoryKey.self] }
        set { self[MessagesRepositoryKey.self] = newValue }
    }
}

@main
struct TaskBridgeApp: App {
    private let taskBridge: TaskBridge
    private let navigationRepository: NavigationRepository
    private let taskTemplatesRepository: TaskTemplatesRepository
    private let tasksRepository: TasksRepository
    private let remindersRepository: RemindersRepository
    private let messagesRepository: MessagesRepository

    init() {
        let platformDependencies = PlatformDependencies()
        let platformHandlers = CorePlatformHandlers(
            reminderHandler: iOSReminderHandler()
        )
        self.taskBridge = TaskBridge(platformDependencies: platformDependencies, platformHandlers: platformHandlers)

        let navigationInteractor = taskBridge.navigationInteractor()
        self.navigationRepository = NavigationRepositoryImpl(interactor: navigationInteractor)

        let templatesInteractor = taskBridge.templatesInteractor()
        self.taskTemplatesRepository = TaskTemplatesRepositoryImpl(interactor: templatesInteractor)

        let tasksInteractor = taskBridge.tasksInteractor()
        self.tasksRepository = TasksRepositoryImpl(interactor: tasksInteractor)

        let remindersInteractor = taskBridge.remindersInteractor()
        self.remindersRepository = RemindersRepositoryImpl(interactor: remindersInteractor)

        let messagesInteractor = taskBridge.messagesInteractor()
        self.messagesRepository = MessagesRepositoryImpl(interactor: messagesInteractor)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.navigationRepository, navigationRepository)
                .environment(\.taskTemplatesRepository, taskTemplatesRepository)
                .environment(\.tasksRepository, tasksRepository)
                .environment(\.remindersRepository, remindersRepository)
                .environment(\.messagesRepository, messagesRepository)
        }
    }
}
