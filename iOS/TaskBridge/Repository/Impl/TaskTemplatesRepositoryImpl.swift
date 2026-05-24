import Foundation
import TaskBridgeCore

@MainActor
class TaskTemplatesRepositoryImpl: TaskTemplatesRepository {
    private let interactor: TemplatesInteractor
    
    init(interactor: TemplatesInteractor) {
        self.interactor = interactor
    }
    
    var templatesState: AsyncStream<TaskTemplatesState> {
        return createStream(for: interactor.templatesState)
    }
    
    func loadTemplates() async throws {
        try await interactor.loadTemplates()
    }
    
    func forceLoadTemplates() async throws {
        try await interactor.forceLoadTemplates()
    }
}
