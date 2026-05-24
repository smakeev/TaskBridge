import Foundation
import TaskBridgeCore
import Combine

@MainActor
protocol NavigationRepository {
    var activePath: AsyncStream<NavigationPath?> { get }
    var currentTab: AsyncStream<AppTab> { get }
    var overlay: AsyncStream<NavigationOverlay?> { get }
    
    func selectTab(tab: AppTab) async throws
    func pushDestination(_ destination: NavigationDestination) async throws
    func popDestination() async throws
    func pullToRoot(tab: AppTab) async throws
}
