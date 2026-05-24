import SwiftUI
import TaskBridgeCore

struct TasksRootView: View {
    @StateObject private var viewModel: TasksViewModel
    private let navigationRepository: NavigationRepository

    @State private var isShowingCreateSheet = false
    @State private var taskToRename: TaskItem?

    init(tasksRepository: TasksRepository, navigationRepository: NavigationRepository) {
        _viewModel = StateObject(wrappedValue: TasksViewModel(repository: tasksRepository))
        self.navigationRepository = navigationRepository
    }

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {
                header

                if viewModel.state.isLoading && viewModel.state.tasks.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding(.top, 48)
                }

                if let error = viewModel.state.errorMessage {
                    Text(error)
                        .font(.callout)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(Color.red.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
                }

                if !viewModel.state.isLoading && viewModel.state.tasks.isEmpty {
                    emptyState
                }

                ForEach(viewModel.state.tasks, id: \.taskIdValue) { task in
                    TaskTreeRowsView(
                        task: task,
                        depth: 0,
                        onOpen: openTaskDetails,
                        onToggleCheckbox: viewModel.toggleCheckbox,
                        onProgressChanged: viewModel.updateProgress,
                        onRename: { taskToRename = $0 },
                        onDelete: { viewModel.deleteTaskTree(taskId: $0.id) }
                    )
                }
            }
            .padding(16)
        }
        .background(Color(.systemGroupedBackground))
        .overlay(alignment: .bottomTrailing) {
            Button {
                isShowingCreateSheet = true
            } label: {
                Image(systemName: "plus")
                    .font(.headline)
                    .frame(width: 54, height: 54)
                    .background(Color.accentColor, in: Circle())
                    .foregroundColor(.white)
                    .shadow(radius: 4, y: 2)
            }
            .padding()
        }
        .task {
            viewModel.loadTasks()
        }
        .sheet(isPresented: $isShowingCreateSheet) {
            TaskEditSheet(title: "New Task") { title, type in
                viewModel.createTask(title: title, type: type)
            }
        }
        .sheet(isPresented: Binding(
            get: { taskToRename != nil },
            set: { if !$0 { taskToRename = nil } }
        )) {
            if let task = taskToRename {
                TaskRenameSheet(task: task) { title in
                    viewModel.renameTask(task: task, newTitle: title)
                    taskToRename = nil
                }
            }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Tasks")
                .font(.largeTitle.weight(.bold))
            Text("Organize work into checklists, progress tasks, and containers.")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, 4)
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Text("No tasks yet")
                .font(.headline)
            Text("Create a root task to start building your tree.")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 56)
    }

    private func openTaskDetails(_ task: TaskItem) {
        Task {
            try? await navigationRepository.pushDestination(NavigationDestinationTaskDetails(taskId: task.taskIdValue))
        }
    }
}
