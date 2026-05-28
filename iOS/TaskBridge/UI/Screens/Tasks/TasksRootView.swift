import SwiftUI
import TaskBridgeCore

struct TasksRootView: View {
    @Environment(\.repositoriesStorage) private var repositoriesStorage

    var body: some View {
        if let repositoriesStorage {
            TasksRootContent(
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

private struct TasksRootContent: View {
    @StateObject private var viewModel: TasksViewModel

    @State private var isShowingCreateSheet = false
    @State private var taskToRename: TaskItem?
    @State private var taskForReminder: TaskItem?
    @StateObject private var highlighter = ScrollBlinkHighlighter()

    init(
        tasksRepository: TasksRepository,
        remindersRepository: RemindersRepository,
        navigationRepository: NavigationRepository,
        messagesRepository: MessagesRepository
    ) {
        _viewModel = StateObject(wrappedValue: TasksViewModel(
            repository: tasksRepository,
            remindersRepository: remindersRepository,
            navigationRepository: navigationRepository,
            messagesRepository: messagesRepository
        ))
    }

    var body: some View {
        ScrollViewReader { proxy in
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
                            highlightedTaskId: highlighter.highlightedId,
                            highlightOpacity: highlighter.opacity,
                            onOpen: openTaskDetails,
                            onToggleCheckbox: viewModel.toggleCheckbox,
                            onProgressChanged: viewModel.updateProgress,
                            onAddReminder: { taskForReminder = $0 },
                            onRename: { taskToRename = $0 },
                            onDelete: { viewModel.deleteTaskTree(taskId: $0.id) }
                        )
                        .id(task.taskIdValue)
                    }
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .task {
                await observeTaskCreatedMessages(scrollProxy: proxy)
            }
            .task {
                await consumePendingNavigationMessage(scrollProxy: proxy)
            }
            .onChange(of: viewModel.state.tasks.map(\.taskIdValue)) { _ in
                guard let highlightedTaskId = highlighter.highlightedId else { return }
                withAnimation(.easeInOut(duration: 0.35)) {
                    proxy.scrollTo(highlightedTaskId, anchor: .center)
                }
            }
        }
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
        viewModel.openTaskDetails(task)
    }

    private func observeTaskCreatedMessages(scrollProxy: ScrollViewProxy) async {
        for await message in viewModel.observeTaskCreatedMessages() {
            guard let taskAdded = message as? AppMessageTaskAdded else { continue }
            guard taskAdded.parentPath.isEmpty else { continue }
            guard (try? await viewModel.isCurrentTasksRoot()) == true else { continue }
            await highlighter.scrollToAndBlink(
                id: taskAdded.id.value,
                scrollProxy: scrollProxy,
                waitForItem: waitForTaskRow
            )
        }
    }

    private func consumePendingNavigationMessage(scrollProxy: ScrollViewProxy) async {
        guard let message = try? await viewModel.consumeNavigationDestinationMessage(
            scopeId: NavigationDestinationMessageScopeId.tasksRoot.scopeId
        ) else {
            return
        }

        if let elementId = message as? NavigationDestinationMessageElementId {
            await highlighter.scrollToAndBlink(
                id: elementId.value,
                scrollProxy: scrollProxy,
                presentationDelayNanoseconds: 1_200_000_000,
                waitForItem: waitForTaskRow
            )
        } else if let taskElement = message as? NavigationDestinationMessageTaskElement,
                  taskElement.parentPath.isEmpty {
            await highlighter.scrollToAndBlink(
                id: taskElement.taskId,
                scrollProxy: scrollProxy,
                presentationDelayNanoseconds: 1_200_000_000,
                waitForItem: waitForTaskRow
            )
        }
    }

    @MainActor
    private func waitForTaskRow(id: String) async -> Bool {
        for _ in 0..<40 {
            if viewModel.state.tasks.contains(where: { $0.taskIdValue == id }) {
                return true
            }
            try? await Task.sleep(nanoseconds: 50_000_000)
        }
        return false
    }

}
