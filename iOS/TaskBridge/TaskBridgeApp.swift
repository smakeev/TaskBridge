import SwiftUI
import TaskBridgeCore

@main
struct TaskBridgeApp: App {
    private let repositoriesStorage: RepositoriesStorage

    init() {
        let platformDependencies = PlatformDependencies()
        let platformHandlers = CorePlatformHandlers(
            reminderHandler: iOSReminderHandler()
        )
        let taskBridge = TaskBridge(platformDependencies: platformDependencies, platformHandlers: platformHandlers)
        self.repositoriesStorage = RepositoriesStorage.create(taskBridge: taskBridge)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.repositoriesStorage, repositoriesStorage)
        }
    }
}
