import SwiftUI
import TaskBridgeCore

struct RemindersRootView: View {
    @Environment(\.repositoriesStorage) private var repositoriesStorage

    var body: some View {
        if let repositoriesStorage {
            RemindersRootContent(
                repository: repositoriesStorage.remindersRepository,
                navigationRepository: repositoriesStorage.navigationRepository,
                messagesRepository: repositoriesStorage.messagesRepository
            )
        } else {
            ProgressView("Initializing...")
        }
    }
}

private struct RemindersRootContent: View {
    @StateObject private var viewModel: RemindersViewModel

    @StateObject private var highlighter = ScrollBlinkHighlighter()

    init(
        repository: RemindersRepository,
        navigationRepository: NavigationRepository,
        messagesRepository: MessagesRepository
    ) {
        _viewModel = StateObject(wrappedValue: RemindersViewModel(
            repository: repository,
            navigationRepository: navigationRepository,
            messagesRepository: messagesRepository
        ))
    }

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 12) {
                    header

                    if viewModel.state.isLoading && viewModel.state.reminders.isEmpty {
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

                    if !viewModel.state.isLoading && viewModel.state.reminders.isEmpty {
                        emptyState
                    }

                    ForEach(viewModel.state.reminders, id: \.reminderIdValue) { reminder in
                        reminderCard(reminder)
                            .id(reminder.reminderIdValue)
                    }
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .task {
                await observeReminderCreatedMessages(scrollProxy: proxy)
            }
            .task {
                await consumePendingNavigationMessage(scrollProxy: proxy)
            }
            .onChange(of: viewModel.state.reminders.map(\.reminderIdValue)) { _ in
                guard let highlightedReminderId = highlighter.highlightedId else { return }
                withAnimation(.easeInOut(duration: 0.35)) {
                    proxy.scrollTo(highlightedReminderId, anchor: .center)
                }
            }
        }
        .task {
            viewModel.loadReminders()
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Reminders")
                .font(.largeTitle.weight(.bold))
            Text("Add reminders from the Tasks tab. A task can have multiple reminders.")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, 4)
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Image(systemName: "bell")
                .font(.title2)
                .foregroundColor(.secondary)
            Text("No reminders yet")
                .font(.headline)
            Text("Open a task and use the reminder button to add one.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 56)
    }

    private func reminderCard(_ reminder: Reminder) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "bell.fill")
                .font(.title3)
                .foregroundColor(.accentColor)
                .frame(width: 24)
                .padding(.top, 2)

            VStack(alignment: .leading, spacing: 4) {
                Text(reminder.title)
                    .font(.headline)
                if !reminder.body.isEmpty {
                    Text(reminder.body)
                        .font(.subheadline)
                }
                Text("\(reminder.type.displayName) • \(formatReminderTime(reminder.triggerAtMillis))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Button(role: .destructive) {
                viewModel.cancelReminder(reminder)
            } label: {
                Image(systemName: "trash")
            }
            .buttonStyle(.plain)
        }
        .padding(14)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .background(
            Color.accentColor.opacity(highlighter.highlightedId == reminder.reminderIdValue ? 0.22 * highlighter.opacity : 0),
            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(
                    highlighter.highlightedId == reminder.reminderIdValue ? Color.accentColor.opacity(0.75 * highlighter.opacity) : Color.primary.opacity(0.06),
                    lineWidth: highlighter.highlightedId == reminder.reminderIdValue ? 2 : 1
                )
        }
    }

    private func observeReminderCreatedMessages(scrollProxy: ScrollViewProxy) async {
        for await message in viewModel.observeReminderCreatedMessages() {
            guard let reminderCreated = message as? AppMessageReminderCreated else { continue }
            await highlighter.scrollToAndBlink(id: reminderCreated.id.value, scrollProxy: scrollProxy)
        }
    }

    private func consumePendingNavigationMessage(scrollProxy: ScrollViewProxy) async {
        guard let message = try? await viewModel.consumeNavigationDestinationMessage(
            scopeId: NavigationDestinationMessageScopeId.remindersRoot.scopeId
        ),
              let elementId = message as? NavigationDestinationMessageElementId else {
            return
        }

        await highlighter.scrollToAndBlink(id: elementId.value, scrollProxy: scrollProxy)
    }

    private func formatReminderTime(_ triggerAtMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(triggerAtMillis) / 1000)
        return Self.dateFormatter.string(from: date)
    }

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()
}

private extension ReminderType {
    var displayName: String {
        switch self {
        case .start:
            return "Start"
        case .deadline:
            return "Deadline"
        default:
            return "Reminder"
        }
    }
}
