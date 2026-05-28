import Foundation
import TaskBridgeCore

@MainActor
class MessagesRepositoryImpl: MessagesRepository {
    private let interactor: MessagesInteractor

    init(interactor: MessagesInteractor) {
        self.interactor = interactor
    }

    func observeAll() -> AsyncStream<AppMessage> {
        return createStream(for: interactor.observeAll())
    }

    func observe(types: [AppMessageKey]) -> AsyncStream<AppMessage> {
        return createStream(for: interactor.observe(types: types))
    }

    func observe(type: AppMessageKey) -> AsyncStream<AppMessage> {
        return createStream(for: interactor.observe(type: type))
    }

    func publish(message: AppMessage) async throws {
        try await interactor.publish(message: message)
    }
}
