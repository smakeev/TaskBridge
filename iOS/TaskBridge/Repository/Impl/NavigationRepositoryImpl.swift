import Foundation
import TaskBridgeCore

@MainActor
class NavigationRepositoryImpl: NavigationRepository {
    private let interactor: NavigationInteractor
    
    init(interactor: NavigationInteractor) {
        self.interactor = interactor
    }
    
    var activePath: AsyncStream<NavigationPath?> {
        return AsyncStream { continuation in
            Task { @MainActor in
                do {
                    try await interactor.activePath.collect(collector: Collector<NavigationPath?> { (value: NavigationPath?) in
                        continuation.yield(value)
                    })
                    continuation.finish()
                } catch {
                    continuation.finish()
                }
            }
        }
    }
    
    var currentTab: AsyncStream<AppTab> {
        return AsyncStream { continuation in
            Task { @MainActor in
                do {
                    try await interactor.currentTab.collect(collector: Collector<AppTab> { (value: AppTab) in
                        continuation.yield(value)
                    })
                    continuation.finish()
                } catch {
                    continuation.finish()
                }
            }
        }
    }
    
    var overlay: AsyncStream<NavigationOverlay?> {
        return AsyncStream { continuation in
            Task { @MainActor in
                do {
                    try await interactor.overlay.collect(collector: Collector<NavigationOverlay?> { (value: NavigationOverlay?) in
                        continuation.yield(value)
                    })
                    continuation.finish()
                } catch {
                    continuation.finish()
                }
            }
        }
    }
    
    func selectTab(tab: AppTab) async throws {
        try await interactor.selectTab(tab: tab)
    }
}

private class Collector<T>: Kotlinx_coroutines_coreFlowCollector {
    private let onEmit: (T) -> Void
    
    init(_ onEmit: @escaping (T) -> Void) {
        self.onEmit = onEmit
    }
    
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let tValue = value as? T {
            onEmit(tValue)
        }
        completionHandler(nil)
    }
}
