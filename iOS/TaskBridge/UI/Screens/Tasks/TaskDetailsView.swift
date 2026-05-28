import SwiftUI
import TaskBridgeCore

struct TaskDetailsView: View {
    let taskId: String

    @Environment(\.tasksRepository) private var tasksRepository
    @Environment(\.remindersRepository) private var remindersRepository
    @Environment(\.navigationRepository) private var navigationRepository
    @Environment(\.messagesRepository) private var messagesRepository

    var body: some View {
        if let tasksRepository,
           let remindersRepository,
           let navigationRepository,
           let messagesRepository {
            TaskDetailsContent(
                taskId: taskId,
                tasksRepository: tasksRepository,
                remindersRepository: remindersRepository,
                navigationRepository: navigationRepository,
                messagesRepository: messagesRepository
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
            .task {
                await observeTaskCreatedMessages(scrollProxy: proxy)
            }
            .task {
                await consumePendingNavigationMessage(scrollProxy: proxy)
            }
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
        .sheet(isPresented: Binding(
            get: { taskForReminder != nil },
            set: { if !$0 { taskForReminder = nil } }
        )) {
            if let task = taskForReminder {
                TaskReminderSheet(task: task) { title, body, type, minutesFromNow in
                    viewModel.createReminder(
                        task: task,
                        title: title,
                        body: body,
                        type: type,
                        minutesFromNow: minutesFromNow
                    )
                    taskForReminder = nil
                }
            }
        }
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

    private func observeTaskCreatedMessages(scrollProxy: ScrollViewProxy) async {
        for await message in viewModel.observeTaskCreatedMessages() {
            guard let taskAdded = message as? AppMessageTaskAdded,
                  taskAdded.parentPath.last?.value == taskId else {
                continue
            }
            guard (try? await viewModel.isCurrentTaskDetails(taskId: taskId)) == true else { continue }
            await highlighter.scrollToAndBlink(
                id: taskAdded.id.value,
                scrollProxy: scrollProxy,
                waitForItem: waitForTaskRow
            )
        }
    }

    private func consumePendingNavigationMessage(scrollProxy: ScrollViewProxy) async {
        guard let message = try? await viewModel.consumeNavigationDestinationMessage(
            scopeId: NavigationDestinationMessageScopeId.taskDetails(parentId: taskId).scopeId
        ),
              let taskElement = message as? NavigationDestinationMessageTaskElement,
              taskElement.parentPath.last == taskId else {
            return
        }

        await highlighter.scrollToAndBlink(
            id: taskElement.taskId,
            scrollProxy: scrollProxy,
            presentationDelayNanoseconds: 1_200_000_000,
            waitForItem: waitForTaskRow
        )
    }

    @MainActor
    private func waitForTaskRow(id: String) async -> Bool {
        for _ in 0..<40 {
            let task = viewModel.state.findTask(id: TaskId(value: taskId))
            if task?.children.contains(where: { $0.taskIdValue == id }) == true {
                return true
            }
            try? await Task.sleep(nanoseconds: 50_000_000)
        }
        return false
    }

}
