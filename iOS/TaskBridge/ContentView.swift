import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    @Environment(\.navigationRepository) private var navigationRepository
    @Environment(\.taskTemplatesRepository) private var templatesRepository
    @Environment(\.tasksRepository) private var tasksRepository
    @Environment(\.remindersRepository) private var remindersRepository
    @State private var selectedTab: AppTab = .tasks
    
    var body: some View {
        Group {
            if let navRepo = navigationRepository,
               let templatesRepo = templatesRepository,
               let tasksRepo = tasksRepository,
               let remindersRepo = remindersRepository {
                TabView(selection: $selectedTab) {
                    TasksNavigationView(
                        navigationRepository: navRepo,
                        tasksRepository: tasksRepo,
                        remindersRepository: remindersRepo
                    )
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.tasks.iconKey))
                        }
                        .tag(AppTab.tasks)
                    
                    TemplatesNavigationView(
                        navigationRepository: navRepo,
                        templatesRepository: templatesRepo,
                        tasksRepository: tasksRepo
                    )
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.templates.iconKey))
                        }
                        .tag(AppTab.templates)
                    
                    RemindersNavigationView(
                        navigationRepository: navRepo,
                        remindersRepository: remindersRepo
                    )
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.reminders.iconKey))
                        }
                        .tag(AppTab.reminders)
                }
                .onChange(of: selectedTab) { newTab in
                    Task {
                        try? await navRepo.selectTab(tab: newTab)
                    }
                }
                .task {
                    // Observe tab changes from Core
                    for await tab in navRepo.currentTab {
                        if selectedTab != tab {
                            selectedTab = tab
                        }
                    }
                }
            } else {
                ProgressView("Initializing...")
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
}
