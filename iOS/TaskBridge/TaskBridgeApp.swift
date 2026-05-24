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
}

@main
struct TaskBridgeApp: App {
    private let taskBridge: TaskBridge
    private let navigationRepository: NavigationRepository
    private let taskTemplatesRepository: TaskTemplatesRepository
    private let tasksRepository: TasksRepository
    
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
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.navigationRepository, navigationRepository)
                .environment(\.taskTemplatesRepository, taskTemplatesRepository)
                .environment(\.tasksRepository, tasksRepository)
        }
    }
}
