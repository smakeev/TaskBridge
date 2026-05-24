import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    private let repository: NavigationRepository
    @State private var selectedTab: AppTab = .tasks
    
    init() {
        let taskBridge = TaskBridge()
        let interactor = taskBridge.navigationInteractor()
        self.repository = NavigationRepositoryImpl(interactor: interactor)
    }
    
    var body: some View {
        TabView(selection: $selectedTab) {
            TasksNavigationView(repository: repository)
                .tabItem {
                    Image(systemName: getSFSymbol(for: AppTab.tasks.iconKey))
                }
                .tag(AppTab.tasks)
            
            TemplatesNavigationView(repository: repository)
                .tabItem {
                    Image(systemName: getSFSymbol(for: AppTab.templates.iconKey))
                }
                .tag(AppTab.templates)
            
            RemindersNavigationView(repository: repository)
                .tabItem {
                    Image(systemName: getSFSymbol(for: AppTab.reminders.iconKey))
                }
                .tag(AppTab.reminders)
        }
        .onChange(of: selectedTab) { newTab in
            Task {
                try? await repository.selectTab(tab: newTab)
            }
        }
        .task {
            // Observe tab changes from Core
            for await tab in repository.currentTab {
                if selectedTab != tab {
                    selectedTab = tab
                }
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
