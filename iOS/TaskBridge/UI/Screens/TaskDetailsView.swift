import SwiftUI

struct TaskDetailsView: View {
    let taskId: String
    
    var body: some View {
        ScreenPlaceholderView(title: "Task Details", subtitle: "ID: \(taskId)")
    }
}
