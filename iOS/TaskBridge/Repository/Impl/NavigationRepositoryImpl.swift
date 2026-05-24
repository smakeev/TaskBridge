import Foundation
import TaskBridgeCore

class NavigationRepositoryImpl: NavigationRepository {
    private let interactor: NavigationInteractor
    
    init(interactor: NavigationInteractor) {
        self.interactor = interactor
    }
    
    var activePath: AsyncStream<NavigationPath?> {
        return AsyncStream { continuation in
            let task = Task {
                for await path in interactor.activePath {
                    continuation.yield(path)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }
    
    var currentTab: AsyncStream<AppTab> {
        return AsyncStream { continuation in
            let task = Task {
                for await tab in interactor.currentTab {
                    continuation.yield(tab)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }
    
    var overlay: AsyncStream<NavigationOverlay?> {
        return AsyncStream { continuation in
            let task = Task {
                for await overlay in interactor.overlay {
                    continuation.yield(overlay)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }
    
    func selectTab(tab: AppTab) async throws {
        try await interactor.selectTab(tab: tab)
    }
}
