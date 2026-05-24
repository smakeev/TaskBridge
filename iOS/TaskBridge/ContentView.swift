import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    private let navigationInteractor: NavigationInteractor
    @State private var selectedTab: AppTab = .tasks
    
    init() {
        let taskBridge = TaskBridge()
        self.navigationInteractor = taskBridge.navigationInteractor()
    }
    
    var body: some View {
        TabView(selection: $selectedTab) {
            ForEach(AppTab.companion.allCases, id: \.self) { tab in
                Text("Content for \(tab.titleKey)")
                    .tabItem {
                        Image(systemName: getSFSymbol(for: tab.iconKey))
                    }
                    .tag(tab)
            }
        }
        .onChange(of: selectedTab) { newTab in
            Task {
                try? await navigationInteractor.selectTab(tab: newTab)
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
