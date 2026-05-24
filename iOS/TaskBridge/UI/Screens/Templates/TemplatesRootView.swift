import SwiftUI
import TaskBridgeCore

struct TemplatesRootView: View {
    @StateObject private var viewModel: TaskTemplatesViewModel
    
    init(
        repository: TaskTemplatesRepository,
        tasksRepository: TasksRepository,
        navigationRepository: NavigationRepository
    ) {
        _viewModel = StateObject(wrappedValue: TaskTemplatesViewModel(
            repository: repository,
            tasksRepository: tasksRepository,
            navigationRepository: navigationRepository
        ))
    }
    
    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 14) {
                header
                
                if viewModel.state.isLoading && viewModel.state.templates.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding(.top, 48)
                } else {
                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .font(.callout)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding()
                            .background(Color.red.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
                    }
                    
                    let templates = viewModel.state.templates.map { $0 }
                    ForEach(templates, id: \.rootTask.id) { template in
                        TemplateCardView(
                            template: template,
                            onAddTask: viewModel.addTask
                        )
                    }
                }
            }
            .padding(16)
        }
        .background(Color(.systemGroupedBackground))
        .refreshable {
            await viewModel.refreshTemplates()
        }
        .task {
            viewModel.loadTemplates()
        }
    }
    
    private var header: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Templates")
                .font(.largeTitle.weight(.bold))
            Text("Reusable task structures ready for upcoming creation flows.")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, 4)
    }
}

private struct TemplateCardView: View {
    let template: TaskTemplate
    let onAddTask: (TaskTemplate, String) -> Void
    @State private var expandedNodeIds: Set<String> = []
    @State private var isShowingNameInput = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(template.title)
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text(template.description_)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Button {
                    isShowingNameInput = true
                } label: {
                    Image(systemName: "plus")
                        .font(.headline)
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.bordered)
                .accessibilityLabel("Add task from template")
            }
            
            Divider()
            
            TemplateTreeView(rootTask: template.rootTask, expandedNodeIds: $expandedNodeIds)
        }
        .padding(16)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 1)
        }
        .onAppear {
            expandedNodeIds.insert(template.rootTask.id)
        }
        .sheet(isPresented: $isShowingNameInput) {
            TemplateTitleInputSheet(initialTitle: template.title) { title in
                onAddTask(template, title)
                isShowingNameInput = false
            }
        }
    }
}

private struct TemplateTitleInputSheet: View {
    let initialTitle: String
    let onAdd: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var title: String

    init(initialTitle: String, onAdd: @escaping (String) -> Void) {
        self.initialTitle = initialTitle
        self.onAdd = onAdd
        _title = State(initialValue: initialTitle)
    }

    var body: some View {
        NavigationStack {
            Form {
                TextField("Title", text: $title)
            }
            .navigationTitle("Task title")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        onAdd(title)
                        dismiss()
                    }
                    .disabled(title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
