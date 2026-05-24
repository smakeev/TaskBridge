import SwiftUI
import TaskBridgeCore

struct TasksRootView: View {
    var body: some View {
        ScreenPlaceholder(title: "Tasks Root")
    }
}

struct TemplatesRootView: View {
    var body: some View {
        ScreenPlaceholder(title: "Templates Root")
    }
}

struct RemindersRootView: View {
    var body: some View {
        ScreenPlaceholder(title: "Reminders Root")
    }
}

struct TaskDetailsView: View {
    let taskId: String
    var body: some View {
        ScreenPlaceholder(title: "Task Details", subtitle: "ID: \(taskId)")
    }
}

struct CreateTaskView: View {
    let parentTaskId: String?
    var body: some View {
        ScreenPlaceholder(title: "Create Task", subtitle: "Parent ID: \(parentTaskId ?? "None")")
    }
}

struct TemplateNameInputView: View {
    let templateId: String
    var body: some View {
        ScreenPlaceholder(title: "Template Name Input", subtitle: "ID: \(templateId)")
    }
}

struct ReminderDetailsView: View {
    let reminderId: String
    var body: some View {
        ScreenPlaceholder(title: "Reminder Details", subtitle: "ID: \(reminderId)")
    }
}

private struct ScreenPlaceholder: View {
    let title: String
    var subtitle: String? = nil
    
    var body: some View {
        VStack(spacing: 8) {
            Text(title)
                .font(.headline)
            if let subtitle = subtitle {
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
