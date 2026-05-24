import Foundation
import TaskBridgeCore

@MainActor
class NavigationRepositoryImpl: NavigationRepository {
    private let interactor: NavigationInteractor
    
    init(interactor: NavigationInteractor) {
        self.interactor = interactor
    }
    
    var activePath: AsyncStream<NavigationPath?> {
        return createStream(for: interactor.activePath)
    }
    
    var currentTab: AsyncStream<AppTab> {
        return createStream(for: interactor.currentTab)
    }
    
    var overlay: AsyncStream<NavigationOverlay?> {
        return createStream(for: interactor.overlay)
    }
    
    func selectTab(tab: AppTab) async throws {
        try await interactor.selectTab(tab: tab)
    }

    func pushDestination(_ destination: NavigationDestination) async throws {
        try await interactor.pushDestination(destination: destination)
    }

    func popDestination() async throws {
        try await interactor.popDestination()
    }

    func pullToRoot(tab: AppTab) async throws {
        try await interactor.pullToRoot(tab: tab)
    }
}
