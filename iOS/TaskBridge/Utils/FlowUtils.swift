import Foundation
import TaskBridgeCore

/**
 * Utility to bridge Kotlin Flows to Swift AsyncStreams with proper lifecycle management.
 */
@MainActor
func createStream<T>(for flow: any Kotlinx_coroutines_coreFlow) -> AsyncStream<T> {
    return AsyncStream { continuation in
        let task = Task { @MainActor in
            do {
                try await flow.collect(collector: Collector<T> { value in
                    continuation.yield(value)
                })
                continuation.finish()
            } catch {
                continuation.finish()
            }
        }
        
        continuation.onTermination = { _ in
            task.cancel()
        }
    }
}

/**
 * Internal collector implementation to bridge Kotlin FlowCollector.
 */
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
