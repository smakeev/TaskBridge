import SwiftUI
import TaskBridgeCore

struct TaskEditSheet: View {
    let title: String
    let onSave: (String, TaskType) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var taskTitle = ""
    @State private var selectedType: TaskType = .checkbox
    
    var body: some View {
        NavigationStack {
            Form {
                TextField("Title", text: $taskTitle)
                Picker("Type", selection: $selectedType) {
                    Text("Checkbox").tag(TaskType.checkbox)
                    Text("Progress").tag(TaskType.progress)
                    Text("Container").tag(TaskType.container)
                }
                .pickerStyle(.segmented)
            }
            .navigationTitle(title)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(taskTitle, selectedType)
                        dismiss()
                    }
                }
            }
        }
    }
}

struct TaskRenameSheet: View {
    let task: TaskItem
    let onSave: (String) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var title: String
    
    init(task: TaskItem, onSave: @escaping (String) -> Void) {
        self.task = task
        self.onSave = onSave
        _title = State(initialValue: task.title)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                TextField("Title", text: $title)
            }
            .navigationTitle("Rename Task")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(title)
                        dismiss()
                    }
                }
            }
        }
    }
}
