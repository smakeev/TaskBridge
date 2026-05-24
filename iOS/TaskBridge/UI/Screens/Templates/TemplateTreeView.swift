import SwiftUI
import TaskBridgeCore

struct TemplateTreeView: View {
    let rootTask: TemplateTaskItem
    @Binding var expandedNodeIds: Set<String>
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            TemplateTaskRow(
                task: rootTask,
                depth: 0,
                expandedNodeIds: $expandedNodeIds
            )
        }
    }
}

private struct TemplateTaskRow: View {
    let task: TemplateTaskItem
    let depth: Int
    @Binding var expandedNodeIds: Set<String>
    
    private var children: [TemplateTaskItem] {
        task.children ?? []
    }
    
    private var isContainer: Bool {
        task.type == .container
    }
    
    private var isExpanded: Bool {
        expandedNodeIds.contains(task.id)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            rowContent
                .contentShape(Rectangle())
                .onTapGesture {
                    guard isContainer else { return }
                    if isExpanded {
                        expandedNodeIds.remove(task.id)
                    } else {
                        expandedNodeIds.insert(task.id)
                    }
                }
            
            if isContainer && isExpanded {
                ForEach(children, id: \.id) { child in
                    TemplateTaskRow(
                        task: child,
                        depth: depth + 1,
                        expandedNodeIds: $expandedNodeIds
                    )
                }
            }
        }
    }
    
    private var rowContent: some View {
        HStack(alignment: .center, spacing: 8) {
            if isContainer {
                Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                    .font(.caption.weight(.semibold))
                    .foregroundColor(.secondary)
                    .frame(width: 16, height: 16)
            } else {
                Color.clear
                    .frame(width: 16, height: 16)
            }
            
            Image(systemName: iconName)
                .font(.subheadline)
                .foregroundColor(iconColor)
                .frame(width: 20)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(task.title)
                    .font(.subheadline.weight(depth == 0 ? .semibold : .regular))
                    .foregroundColor(.primary)
                    .lineLimit(2)
                
                if task.type == .progress, let progress = task.initialProgress {
                    Text("\(progress)% initial progress")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, 4)
        .padding(.leading, indentation)
    }
    
    private var indentation: CGFloat {
        CGFloat(min(depth, 4)) * 18
    }
    
    private var iconName: String {
        switch task.type {
        case .checkbox:
            return "checkmark.circle"
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
            return .green
        case .progress:
            return .blue
        case .container:
            return .orange
        default:
            return .secondary
        }
    }
}
