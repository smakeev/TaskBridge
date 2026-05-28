import SwiftUI
import TaskBridgeCore

struct TaskTreeRowsView: View {
    let task: TaskItem
    let depth: Int
    let highlightedTaskId: String?
    let highlightOpacity: Double
    let onOpen: (TaskItem) -> Void
    let onToggleCheckbox: (TaskItem) -> Void
    let onProgressChanged: (TaskItem, Int) -> Void
    let onAddReminder: (TaskItem) -> Void
    let onRename: (TaskItem) -> Void
    let onDelete: (TaskItem) -> Void

    init(
        task: TaskItem,
        depth: Int,
        highlightedTaskId: String? = nil,
        highlightOpacity: Double = 0,
        onOpen: @escaping (TaskItem) -> Void,
        onToggleCheckbox: @escaping (TaskItem) -> Void,
        onProgressChanged: @escaping (TaskItem, Int) -> Void,
        onAddReminder: @escaping (TaskItem) -> Void,
        onRename: @escaping (TaskItem) -> Void,
        onDelete: @escaping (TaskItem) -> Void
    ) {
        self.task = task
        self.depth = depth
        self.highlightedTaskId = highlightedTaskId
        self.highlightOpacity = highlightOpacity
        self.onOpen = onOpen
        self.onToggleCheckbox = onToggleCheckbox
        self.onProgressChanged = onProgressChanged
        self.onAddReminder = onAddReminder
        self.onRename = onRename
        self.onDelete = onDelete
    }

    var body: some View {
        TaskRowView(
            task: task,
            depth: depth,
            isHighlighted: highlightedTaskId == task.taskIdValue,
            highlightOpacity: highlightOpacity,
            onOpen: onOpen,
            onToggleCheckbox: onToggleCheckbox,
            onProgressChanged: onProgressChanged,
            onAddReminder: onAddReminder,
            onRename: onRename,
            onDelete: onDelete
        )
    }
}

struct TaskRowView: View {
    let task: TaskItem
    let depth: Int
    let isHighlighted: Bool
    let highlightOpacity: Double
    let onOpen: (TaskItem) -> Void
    let onToggleCheckbox: (TaskItem) -> Void
    let onProgressChanged: (TaskItem, Int) -> Void
    let onAddReminder: (TaskItem) -> Void
    let onRename: (TaskItem) -> Void
    let onDelete: (TaskItem) -> Void

    init(
        task: TaskItem,
        depth: Int,
        isHighlighted: Bool = false,
        highlightOpacity: Double = 0,
        onOpen: @escaping (TaskItem) -> Void,
        onToggleCheckbox: @escaping (TaskItem) -> Void,
        onProgressChanged: @escaping (TaskItem, Int) -> Void,
        onAddReminder: @escaping (TaskItem) -> Void,
        onRename: @escaping (TaskItem) -> Void,
        onDelete: @escaping (TaskItem) -> Void
    ) {
        self.task = task
        self.depth = depth
        self.isHighlighted = isHighlighted
        self.highlightOpacity = highlightOpacity
        self.onOpen = onOpen
        self.onToggleCheckbox = onToggleCheckbox
        self.onProgressChanged = onProgressChanged
        self.onAddReminder = onAddReminder
        self.onRename = onRename
        self.onDelete = onDelete
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            Image(systemName: iconName)
                .font(.title3)
                .foregroundColor(iconColor)
                .frame(width: 24)

            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(.subheadline.weight(depth == 0 ? .semibold : .regular))
                    .foregroundColor(.primary)
                    .lineLimit(2)

                Text(taskStatus(task))
                    .font(.caption)
                    .foregroundColor(.secondary)

                if task.type == .progress {
                    TaskProgressSlider(
                        task: task,
                        onProgressChanged: onProgressChanged
                    )
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if task.type == .checkbox {
                Button {
                    onToggleCheckbox(task)
                } label: {
                    Image(systemName: task.isChecked ? "checkmark.circle.fill" : "circle")
                }
                .buttonStyle(.plain)
                .foregroundColor(task.isChecked ? .green : .secondary)
            }

            Button {
                onAddReminder(task)
            } label: {
                Image(systemName: "bell.badge")
            }
            .buttonStyle(.plain)
            .foregroundColor(.secondary)

            Button {
                onRename(task)
            } label: {
                Image(systemName: "pencil")
            }
            .buttonStyle(.plain)
            .foregroundColor(.secondary)

            Button(role: .destructive) {
                onDelete(task)
            } label: {
                Image(systemName: "trash")
            }
            .buttonStyle(.plain)
        }
        .padding(14)
        .padding(.leading, CGFloat(min(depth, 4)) * 14)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .background(
            Color.accentColor.opacity(isHighlighted ? 0.22 * highlightOpacity : 0),
            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(
                    isHighlighted ? Color.accentColor.opacity(0.75 * highlightOpacity) : Color.primary.opacity(0.06),
                    lineWidth: isHighlighted ? 2 : 1
                )
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onOpen(task)
        }
    }

    private var iconName: String {
        switch task.type {
        case .checkbox:
            return task.isChecked ? "checkmark.circle.fill" : "checkmark.circle"
        case .progress:
            return "chart.bar"
        case .container:
            return "folder"
        default:
            return "circle"
        }
    }

    private var iconColor: Color {
        switch task.type {
        case .checkbox:
            return task.isChecked ? .green : .secondary
        case .progress:
            return .blue
        case .container:
            return .orange
        default:
            return .secondary
        }
    }
}

struct TaskProgressSlider: View {
    let task: TaskItem
    let onProgressChanged: (TaskItem, Int) -> Void

    @State private var value: Double = 0

    var body: some View {
        Slider(
            value: Binding(
                get: { value },
                set: { value = $0 }
            ),
            in: 0...100,
            step: 1,
            onEditingChanged: { isEditing in
                if !isEditing {
                    onProgressChanged(task, Int(value))
                }
            }
        )
        .onAppear {
            value = Double(task.progressValue)
        }
        .onChange(of: task.progressValue) { newValue in
            value = Double(newValue)
        }
    }
}

func taskStatus(_ task: TaskItem) -> String {
    switch task.type {
    case .checkbox:
        return task.isChecked ? "Checked" : "Unchecked"
    case .progress:
        return "\(task.progressValue)% complete"
    case .container:
        return subtaskCountText(task.children.count)
    default:
        return "Task"
    }
}

private func subtaskCountText(_ count: Int) -> String {
    count == 1 ? "1 subtask" : "\(count) subtasks"
}
