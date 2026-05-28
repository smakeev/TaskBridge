import Foundation
import TaskBridgeCore
import Combine

public enum NavigationDestinationMessageScopeId: Equatable {
    case tasksRoot
    case taskDetails(parentId: String)
    case remindersRoot

    public var scopeId: String {
        switch self {
        case .tasksRoot:
            return "tasksRoot"
        case let .taskDetails(parentId):
            return "taskDetails:\(parentId)"
        case .remindersRoot:
            return "remindersRoot"
        }
    }
}

@MainActor
public protocol NavigationRepository {
    var activePath: AsyncStream<NavigationPath?> { get }
    var currentTab: AsyncStream<AppTab> { get }
    var overlay: AsyncStream<NavigationOverlay?> { get }

    func selectTab(tab: AppTab) async throws
    func pushDestination(_ destination: NavigationDestination) async throws
    func popDestination() async throws
    func pullToRoot(tab: AppTab) async throws
    func fetchActivePath() async throws -> NavigationPath?
    func fetchCurrentTab() async throws -> AppTab
    func setNavigationDestinationMessage(_ message: NavigationDestinationMessage?) async throws
    func consumeNavigationDestinationMessage(scopeId: String) async throws -> NavigationDestinationMessage?
}
