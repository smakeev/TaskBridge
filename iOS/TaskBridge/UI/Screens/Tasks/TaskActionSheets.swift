import SwiftUI
import TaskBridgeCore

extension View {
    /// The rename + reminder sheets shared by the Tasks root and details screens.
    /// Task-specific (unlike the generic `scrollBlinkHighlighting`): both are driven
    /// by a selected `TaskItem` binding.
    func taskActionSheets(
        viewModel: TasksViewModel,
        taskToRename: Binding<TaskItem?>,
        taskForReminder: Binding<TaskItem?>
    ) -> some View {
        modifier(TaskActionSheetsModifier(
            viewModel: viewModel,
            taskToRename: taskToRename,
            taskForReminder: taskForReminder
        ))
    }
}

private struct TaskActionSheetsModifier: ViewModifier {
    let viewModel: TasksViewModel
    @Binding var taskToRename: TaskItem?
    @Binding var taskForReminder: TaskItem?

    func body(content: Content) -> some View {
        content
            .sheet(isPresented: Binding(
                get: { taskToRename != nil },
                set: { if !$0 { taskToRename = nil } }
            )) {
                if let task = taskToRename {
                    TaskRenameSheet(task: task) { title in
                        viewModel.renameTask(task: task, newTitle: title)
                        taskToRename = nil
                    }
                }
            }
            .sheet(isPresented: Binding(
                get: { taskForReminder != nil },
                set: { if !$0 { taskForReminder = nil } }
            )) {
                if let task = taskForReminder {
                    TaskReminderSheet(task: task) { title, body, type, minutesFromNow in
                        viewModel.createReminder(
                            task: task,
                            title: title,
                            body: body,
                            type: type,
                            minutesFromNow: minutesFromNow
                        )
                        taskForReminder = nil
                    }
                }
            }
    }
}
