import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    @Environment(\.navigationRepository) private var navigationRepository
    @Environment(\.taskTemplatesRepository) private var templatesRepository
    @State private var selectedTab: AppTab = .tasks
    
    var body: some View {
        Group {
            if let navRepo = navigationRepository, let templatesRepo = templatesRepository {
                TabView(selection: $selectedTab) {
                    TasksNavigationView(repository: navRepo)
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.tasks.iconKey))
                        }
                        .tag(AppTab.tasks)
                    
                    TemplatesNavigationView(navigationRepository: navRepo, templatesRepository: templatesRepo)
                        .tabItem {
                            Image(systemName: getSFSymbol(for: AppTab.templates.iconKey))
                        }
                        .tag(AppTab.templates)
                    
                    RemindersNavigationView(repository: navRepo)
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
