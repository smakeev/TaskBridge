import TaskBridgeCore

public extension NavigationRepository {
    func isCurrentRoot(tab: AppTab) async throws -> Bool {
        let currentTab = try await fetchCurrentTab()
        guard currentTab == tab else { return false }
        let activePath = try await fetchActivePath()
        guard let current = activePath?.current else { return false }

        switch tab {
        case .tasks:
            return current is NavigationDestinationTasksRoot
        case .templates:
            return current is NavigationDestinationTemplatesRoot
        case .reminders:
            return current is NavigationDestinationRemindersRoot
        default:
            return false
        }
    }
}
