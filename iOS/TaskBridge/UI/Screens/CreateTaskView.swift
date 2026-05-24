import SwiftUI

struct CreateTaskView: View {
    let parentTaskId: String?
    
    var body: some View {
        ScreenPlaceholderView(title: "Create Task", subtitle: "Parent ID: \(parentTaskId ?? "None")")
    }
}
