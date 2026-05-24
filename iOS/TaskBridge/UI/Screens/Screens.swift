import SwiftUI
import TaskBridgeCore

struct TasksRootView: View {
    var body: some View {
        ScreenPlaceholder(title: "Tasks Root")
    }
}

struct TemplatesRootView: View {
    @StateObject private var viewModel: TaskTemplatesViewModel
    
    init(repository: TaskTemplatesRepository) {
        _viewModel = StateObject(wrappedValue: TaskTemplatesViewModel(repository: repository))
    }
    
    var body: some View {
        VStack {
            HStack {
                Text("Templates")
                    .font(.largeTitle)
                    .bold()
                Spacer()
            }
            .padding()
            
            if viewModel.state.isLoading && viewModel.state.templates.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List {
                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                    }
                    
                    let templates = viewModel.state.templates.map { $0 }
                    ForEach(templates, id: \.self) { template in
                        Text(template.title)
                            .padding(.vertical, 4)
                    }
                }
                .refreshable {
                    await viewModel.refreshTemplates()
                }
            }
        }
        .task {
            viewModel.loadTemplates()
        }
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
