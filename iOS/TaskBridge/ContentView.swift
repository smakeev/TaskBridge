import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    @Environment(\.repositoriesStorage) private var repositoriesStorage
    @State private var selectedTab: AppTab = .tasks
    @State private var navigationRepository: NavigationRepository?
    @State private var messagesRepository: MessagesRepository?
    @StateObject private var toastPresenter = ToastPresenter()
    
    var body: some View {
        Group {
            if let navRepo = navigationRepository,
               let messagesRepo = messagesRepository {
                TabView(selection: $selectedTab) {
                    TasksNavigationView()
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.tasks.iconKey))
                        }
                        .tag(AppTab.tasks)
                    
                    TemplatesNavigationView()
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.templates.iconKey))
                        }
                        .tag(AppTab.templates)
                    
                    RemindersNavigationView()
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.reminders.iconKey))
                        }
                        .tag(AppTab.reminders)
                }
                    .toastOverlay(text: toastPresenter.text)
                    .onChange(of: selectedTab) { newTab in
                        Task {
                            try? await navRepo.selectTab(tab: newTab)
                        }
                    }
                    .task {
                        // Observe tab changes from Core.
                        for await tab in navRepo.currentTab {
                            if selectedTab != tab {
                                selectedTab = tab
                            }
                        }
                    }
                    .task {
                        for await message in messagesRepo.observe(
                            types: [
                                AppMessageKeys.shared.reminderCreated,
                                AppMessageKeys.shared.taskAdded
                            ]
                        ) {
                            if let toastable = message as? Toastable {
                                showToast(for: toastable)
                            }
                            handleNavigationMessage(message, navigationRepository: navRepo)
                        }
                    }
            } else {
                ProgressView("Initializing...")
            }
        }
        .task {
            if navigationRepository == nil || messagesRepository == nil {
                navigationRepository = repositoriesStorage?.navigationRepository
                messagesRepository = repositoriesStorage?.messagesRepository
            }
        }
    }
    
    private func getSFSymbol(for iconKey: String) -> String {
        switch iconKey {
        case "checklist": return "checklist"
        case "library_books": return "books.vertical"
        case "notifications": return "bell"
        default: return "questionmark"
        }
    }

    private func showToast(for toastable: Toastable) {
        toastPresenter.show(toastable.text)
    }

    private func handleNavigationMessage(
        _ message: AppMessage,
        navigationRepository: NavigationRepository
    ) {
        Task {
            if let reminderCreated = message as? AppMessageReminderCreated {
                if (try? await navigationRepository.isCurrentRoot(tab: .reminders)) == true {
                    return
                }
                try? await navigationRepository.setNavigationDestinationMessage(
                    NavigationDestinationMessageElementId(
                        value: reminderCreated.id.value,
                        scopeId: NavigationDestinationMessageScopeId.remindersRoot.scopeId
                    )
                )
                try? await navigationRepository.pullToRoot(tab: .reminders)
                try? await navigationRepository.selectTab(tab: .reminders)
            } else if let taskAdded = message as? AppMessageTaskAdded {
                if (try? await navigationRepository.fetchCurrentTab()) == .tasks {
                    return
                }
                let parentPath = taskAdded.parentPath.map { $0.value }
                // For now tasks can only be added outside the Tasks tab as root tasks.
                // Later, if subtasks can be created from other tabs, use parentPath to rebuild the full stack.
                try? await navigationRepository.setNavigationDestinationMessage(
                    NavigationDestinationMessageTaskElement(
                        taskId: taskAdded.id.value,
                        parentPath: parentPath,
                        scopeId: NavigationDestinationMessageScopeId.tasksRoot.scopeId
                    )
                )
                try? await navigationRepository.pullToRoot(tab: .tasks)
                try? await navigationRepository.selectTab(tab: .tasks)
            }
        }
    }
}
