import SwiftUI
import TaskBridgeCore

@MainActor
class TaskTemplatesViewModel: ObservableObject {
    @Published var state: TaskTemplatesState = TaskTemplatesState(templates: [], isLoading: false, errorMessage: nil, lastLoadedAtMillis: nil)
    private let repository: TaskTemplatesRepository
    private var observationTask: Task<Void, Never>?
    
    init(repository: TaskTemplatesRepository) {
        self.repository = repository
        
        observationTask = Task {
            for await newState in repository.templatesState {
                self.state = newState
            }
        }
    }
    
    deinit {
        observationTask?.cancel()
    }
    
    func loadTemplates() {
        Task {
            try? await repository.loadTemplates()
        }
    }
    
    func refreshTemplates() async {
        try? await repository.loadTemplates()
    }
}
