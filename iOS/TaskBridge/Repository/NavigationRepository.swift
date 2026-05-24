import Foundation
import TaskBridgeCore
import Combine

protocol NavigationRepository {
    var activePath: AsyncStream<NavigationPath?> { get }
    var currentTab: AsyncStream<AppTab> { get }
    var overlay: AsyncStream<NavigationOverlay?> { get }
    
    func selectTab(tab: AppTab) async throws
}
