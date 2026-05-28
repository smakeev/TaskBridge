import Foundation
import TaskBridgeCore

@MainActor
protocol MessagesRepository {
    func observeAll() -> AsyncStream<AppMessage>
    func observe(types: [AppMessageKey]) -> AsyncStream<AppMessage>
    func observe(type: AppMessageKey) -> AsyncStream<AppMessage>
    func publish(message: AppMessage) async throws
}
