import SwiftUI

struct ReminderDetailsView: View {
    let reminderId: String
    
    var body: some View {
        ScreenPlaceholderView(title: "Reminder Details", subtitle: "ID: \(reminderId)")
    }
}
