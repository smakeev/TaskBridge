import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    let taskBridge = TaskBridge()
    
    var body: some View {
        VStack {
            Text(taskBridge.message)
                .padding()
        }
    }
}
