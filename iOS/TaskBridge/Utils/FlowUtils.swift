import Foundation
import TaskBridgeCore

/// Sleep for the given duration without observing Task cancellation.
/// Useful for pacing UI animations that must complete even if the
/// owning `.task` modifier is cancelled mid-flow.
func uncancellableSleep(nanoseconds: UInt64) async {
    await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
        DispatchQueue.main.asyncAfter(deadline: .now() + .nanoseconds(Int(nanoseconds))) {
            continuation.resume()
        }
    }
}

/**
 * Utility to bridge Kotlin Flows to Swift AsyncStreams with proper lifecycle management.
 * Enforces @MainActor to satisfy Kotlin/Native threading requirements for suspend functions.
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
