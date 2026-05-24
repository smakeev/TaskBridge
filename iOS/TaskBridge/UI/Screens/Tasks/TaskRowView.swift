import SwiftUI
import TaskBridgeCore

struct TaskTreeRowsView: View {
    let task: TaskItem
    let depth: Int
    let onOpen: (TaskItem) -> Void
    let onToggleCheckbox: (TaskItem) -> Void
    let onProgressChanged: (TaskItem, Int) -> Void
    let onRename: (TaskItem) -> Void
    let onDelete: (TaskItem) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            TaskRowView(
                task: task,
                depth: depth,
                onOpen: onOpen,
                onToggleCheckbox: onToggleCheckbox,
                onProgressChanged: onProgressChanged,
                onRename: onRename,
                onDelete: onDelete
            )
            
            ForEach(task.children, id: \.taskIdValue) { child in
                TaskTreeRowsView(
                    task: child,
                    depth: depth + 1,
                    onOpen: onOpen,
                    onToggleCheckbox: onToggleCheckbox,
                    onProgressChanged: onProgressChanged,
                    onRename: onRename,
                    onDelete: onDelete
                )
            }
        }
    }
}

struct TaskRowView: View {
    let task: TaskItem
    let depth: Int
    let onOpen: (TaskItem) -> Void
    let onToggleCheckbox: (TaskItem) -> Void
    let onProgressChanged: (TaskItem, Int) -> Void
    let onRename: (TaskItem) -> Void
    let onDelete: (TaskItem) -> Void
    
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
                    Slider(
                        value: Binding(
                            get: { Double(task.progressValue) },
                            set: { onProgressChanged(task, Int($0)) }
                        ),
                        in: 0...100,
                        step: 1
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
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 1)
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

func taskStatus(_ task: TaskItem) -> String {
    switch task.type {
    case .checkbox:
        return task.isChecked ? "Checked" : "Unchecked"
    case .progress:
        return "\(task.progressValue)% complete"
    case .container:
        return "\(task.children.count) subtasks · \(containerCompletion(task))% complete"
    default:
        return "Task"
    }
}

func containerCompletion(_ task: TaskItem) -> Int {
    guard !task.children.isEmpty else { return 0 }
    let completed = task.children.filter { $0.isCompleted }.count
    return Int((Double(completed) / Double(task.children.count)) * 100)
}
