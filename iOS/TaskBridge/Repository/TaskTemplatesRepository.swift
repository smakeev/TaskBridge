import Foundation
import TaskBridgeCore

@MainActor
protocol TaskTemplatesRepository: AnyObject {
    var templatesState: AsyncStream<TaskTemplatesState> { get }
    func loadTemplates() async throws
    func forceLoadTemplates() async throws
}
