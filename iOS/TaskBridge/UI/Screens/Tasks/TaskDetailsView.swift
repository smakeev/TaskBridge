import SwiftUI
import TaskBridgeCore

struct TaskDetailsView: View {
    let taskId: String
    
    @StateObject private var viewModel: TasksViewModel
    private let navigationRepository: NavigationRepository
    
    @State private var isShowingSubtaskSheet = false
    @State private var taskToRename: TaskItem?
    
    init(taskId: String, tasksRepository: TasksRepository, navigationRepository: NavigationRepository) {
        self.taskId = taskId
        _viewModel = StateObject(wrappedValue: TasksViewModel(repository: tasksRepository))
        self.navigationRepository = navigationRepository
    }
    
    var body: some View {
        let task = viewModel.state.findTask(id: taskId)
        
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {
                header
                
                if viewModel.state.isLoading && task == nil {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding(.top, 48)
                } else if let task {
                    detailsCard(task)
                    
                    if task.type == .container {
                        Button {
                            isShowingSubtaskSheet = true
                        } label: {
                            Label("Add subtask", systemImage: "plus")
                        }
                        .buttonStyle(.borderedProminent)
                        .padding(.top, 4)
                        
                        if task.children.isEmpty {
                            Text("No subtasks yet")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .padding(.vertical, 16)
                        } else {
                            ForEach(task.children, id: \.taskIdValue) { child in
                                TaskTreeRowsView(
                                    task: child,
                                    depth: 0,
                                    onOpen: openTaskDetails,
                                    onToggleCheckbox: viewModel.toggleCheckbox,
                                    onProgressChanged: viewModel.updateProgress,
                                    onRename: { taskToRename = $0 },
                                    onDelete: { viewModel.deleteTaskTree(taskId: $0.id) }
                                )
                            }
                        }
                    }
                } else {
                    Text("Task not found")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.top, 56)
                }
            }
            .padding(16)
        }
        .background(Color(.systemGroupedBackground))
        .task {
            viewModel.loadTasks()
        }
        .sheet(isPresented: $isShowingSubtaskSheet) {
            if let task {
                TaskEditSheet(title: "New Subtask") { title, type in
                    viewModel.createSubtask(parentTask: task, title: title, type: type)
                }
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
        HStack(spacing: 12) {
            Button {
                Task {
                    try? await navigationRepository.popDestination()
                }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.headline)
            }
            .buttonStyle(.plain)
            
            Text("Task Details")
                .font(.title2.weight(.bold))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    private func detailsCard(_ task: TaskItem) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(task.title)
                        .font(.title2.weight(.semibold))
                    Text(taskStatus(task))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Button {
                    taskToRename = task
                } label: {
                    Image(systemName: "pencil")
                }
                .buttonStyle(.plain)
                
                Button(role: .destructive) {
                    viewModel.deleteTaskTree(taskId: task.id)
                    Task {
                        try? await navigationRepository.popDestination()
                    }
                } label: {
                    Image(systemName: "trash")
                }
                .buttonStyle(.plain)
            }
            
            if task.type == .checkbox {
                Toggle("Completed", isOn: Binding(
                    get: { task.isChecked },
                    set: { _ in viewModel.toggleCheckbox(task: task) }
                ))
            }
            
            if task.type == .progress {
                TaskProgressSlider(
                    task: task,
                    onProgressChanged: viewModel.updateProgress
                )
            }
        }
        .padding(16)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
    
    private func openTaskDetails(_ task: TaskItem) {
        Task {
            try? await navigationRepository.pushDestination(NavigationDestinationTaskDetails(taskId: task.taskIdValue))
        }
    }
}
