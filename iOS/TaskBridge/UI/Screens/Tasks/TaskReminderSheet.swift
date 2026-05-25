import SwiftUI
import TaskBridgeCore

struct TaskReminderSheet: View {
    let task: TaskItem
    let onSave: (String, String, ReminderType, Int) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var title: String
    @State private var bodyText = ""
    @State private var type: ReminderType = .start
    @State private var minutesFromNow = 60

    init(task: TaskItem, onSave: @escaping (String, String, ReminderType, Int) -> Void) {
        self.task = task
        self.onSave = onSave
        _title = State(initialValue: task.title)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Title", text: $title)
                    TextField("Body", text: $bodyText, axis: .vertical)
                        .lineLimit(2...4)
                }

                Section {
                    Picker("Type", selection: $type) {
                        Text("Start").tag(ReminderType.start)
                        Text("Deadline").tag(ReminderType.deadline)
                    }
                    Stepper("Remind in \(minutesFromNow) minutes", value: $minutesFromNow, in: 1...10080)
                }

                Section {
                    Text("Add reminders from a task. A task can have multiple reminders.")
                        .font(.footnote)
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Add Reminder")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(title, bodyText, type, minutesFromNow)
                        dismiss()
                    }
                    .disabled(title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
