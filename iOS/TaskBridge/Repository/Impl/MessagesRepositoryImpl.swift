import Foundation
import TaskBridgeCore

@MainActor
class MessagesRepositoryImpl: MessagesRepository {
    private let interactor: MessagesInteractor

    init(interactor: MessagesInteractor) {
        self.interactor = interactor
    }

    func observe(type: any KotlinKClass) -> AsyncStream<AppMessage> {
        return createStream(for: interactor.observe(type: type))
    }

    func publish(message: AppMessage) async throws {
        try await interactor.publish(message: message)
    }
}
