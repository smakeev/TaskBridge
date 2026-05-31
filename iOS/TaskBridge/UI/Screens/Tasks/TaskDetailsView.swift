import SwiftUI
import TaskBridgeCore

struct TaskDetailsView: View {
    let taskId: String

    @Environment(\.repositoriesStorage) private var repositoriesStorage

    var body: some View {
        if let repositoriesStorage {
            TaskDetailsContent(
                taskId: taskId,
                tasksRepository: repositoriesStorage.tasksRepository,
                remindersRepository: repositoriesStorage.remindersRepository,
                navigationRepository: repositoriesStorage.navigationRepository,
                messagesRepository: repositoriesStorage.messagesRepository
            )
        } else {
            ProgressView("Initializing...")
        }
    }
}

private struct TaskDetailsContent: View {
    let taskId: String

    @StateObject private var viewModel: TasksViewModel

    @State private var isShowingSubtaskSheet = false
    @State private var taskToRename: TaskItem?
    @State private var taskForReminder: TaskItem?
    @StateObject private var highlighter = ScrollBlinkHighlighter()

    init(
        taskId: String,
        tasksRepository: TasksRepository,
        remindersRepository: RemindersRepository,
        navigationRepository: NavigationRepository,
        messagesRepository: MessagesRepository
    ) {
        self.taskId = taskId
        _viewModel = StateObject(wrappedValue: TasksViewModel(
            repository: tasksRepository,
            remindersRepository: remindersRepository,
            navigationRepository: navigationRepository,
            messagesRepository: messagesRepository
        ))
    }

    var body: some View {
        let task = viewModel.state.findTask(id: TaskId(value: taskId))

        ScrollViewReader { proxy in
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
                                        highlightedTaskId: highlighter.highlightedId,
                                        highlightOpacity: highlighter.opacity,
                                        onOpen: openTaskDetails,
                                        onToggleCheckbox: viewModel.toggleCheckbox,
                                        onProgressChanged: viewModel.updateProgress,
                                        onAddReminder: { taskForReminder = $0 },
                                        onRename: { taskToRename = $0 },
                                        onDelete: { viewModel.deleteTaskTree(taskId: $0.id) }
                                    )
                                    .id(child.taskIdValue)
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
            .scrollBlinkHighlighting(
                highlighter: highlighter,
                binding: blinkBinding,
                scrollProxy: proxy
            )
            .onChange(of: task?.children.map(\.taskIdValue) ?? []) { _ in
                guard let highlightedTaskId = highlighter.highlightedId else { return }
                withAnimation(.easeInOut(duration: 0.35)) {
                    proxy.scrollTo(highlightedTaskId, anchor: .center)
                }
            }
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
        .taskActionSheets(
            viewModel: viewModel,
            taskToRename: $taskToRename,
            taskForReminder: $taskForReminder
        )
    }

    private var header: some View {
        HStack(spacing: 12) {
            Button {
                viewModel.popDestination()
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
                    taskForReminder = task
                } label: {
                    Image(systemName: "bell.badge")
                }
                .buttonStyle(.plain)

                Button {
                    taskToRename = task
                } label: {
                    Image(systemName: "pencil")
                }
                .buttonStyle(.plain)

                Button(role: .destructive) {
                    viewModel.deleteTaskTree(taskId: task.id)
                    viewModel.popDestination()
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
        viewModel.openTaskDetails(task)
    }

    private var blinkBinding: ScrollBlinkBinding {
        ScrollBlinkBinding(
            scopeId: NavigationDestinationMessageScopeId.taskDetails(parentId: taskId).scopeId,
            createdMessages: { viewModel.observeTaskCreatedMessages() },
            createdTargetId: { message in
                guard let taskAdded = message as? AppMessageTaskAdded,
                      taskAdded.parentPath.last?.value == taskId else {
                    return nil
                }
                guard (try? await viewModel.isCurrentTaskDetails(taskId: taskId)) == true else { return nil }
                return taskAdded.id.value
            },
            consumePending: { try await viewModel.consumeNavigationDestinationMessage(scopeId: $0) },
            pendingTargetId: { message in
                guard let taskElement = message as? NavigationDestinationMessageTaskElement,
                      taskElement.parentPath.last == taskId else {
                    return nil
                }
                return taskElement.taskId
            },
            waitForItem: { id in
                for _ in 0..<40 {
                    let task = viewModel.state.findTask(id: TaskId(value: taskId))
                    if task?.children.contains(where: { $0.taskIdValue == id }) == true {
                        return true
                    }
                    try? await Task.sleep(nanoseconds: 50_000_000)
                }
                return false
            }
        )
    }

}
