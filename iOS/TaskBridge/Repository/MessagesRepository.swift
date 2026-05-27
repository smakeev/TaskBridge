import Foundation
import TaskBridgeCore

@MainActor
protocol MessagesRepository {
    func observe(type: any KotlinKClass) -> AsyncStream<AppMessage>
    func publish(message: AppMessage) async throws
}
